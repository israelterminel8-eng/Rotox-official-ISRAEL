package com.example.engine3d

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.data.AccessoryPart
import com.example.data.AvatarAppearance
import com.example.data.BlockType
import com.example.data.NpcAvatarState
import com.example.data.SkyTheme
import com.example.data.WorldBlock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

enum class RenderMode {
    SHOWCASE,
    ACCESSORY_PREVIEW,
    PLAY,
    BUILD_WORLD
}

data class ProjectedLabel(
    val username: String,
    val screenX: Float,
    val screenY: Float,
    val isFriend: Boolean,
    val chatLine: String
)

class Rotox3DRenderer(
    private val mode: RenderMode,
    private val onCoinsCollectedDelta: (Int) -> Unit = {},
    private val onGoalReached: () -> Unit = {},
    private val onLavaHit: () -> Unit = {},
    private val onProjectedLabels: (List<ProjectedLabel>) -> Unit = {}
) : GLSurfaceView.Renderer {

    private val mesh = CubeMesh()

    private val projMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val vpMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val tmpVec = FloatArray(4)

    @Volatile var viewportWidth = 1
    @Volatile var viewportHeight = 1

    // Scene state snapshots
    @Volatile var avatarAppearance: AvatarAppearance = AvatarAppearance()
    @Volatile var skyTheme: SkyTheme = SkyTheme.DAY
    @Volatile var worldBlocks: List<WorldBlock> = emptyList()
    @Volatile var npcAvatars: List<NpcAvatarState> = emptyList()
    @Volatile var buildCursorPos: Triple<Float, Float, Float>? = null

    // Camera orbit controls
    @Volatile var cameraYawDeg = 25f
    @Volatile var cameraPitchDeg = 22f
    @Volatile var autoRotate = (mode == RenderMode.SHOWCASE)

    // Player physics state (PLAY mode)
    @Volatile var joyX = 0f
    @Volatile var joyY = 0f
    @Volatile private var jumpRequested = false

    private var playerX = 0f
    private var playerY = 1.6f
    private var playerZ = 0f
    private var playerVelY = 0f
    private var playerYawDeg = 180f
    private var isGrounded = false
    private var walkPhase = 0f
    private var lastFrameNanos = System.nanoTime()
    private var elapsedSec = 0f
    private val collectedCoinIndices = mutableSetOf<Int>()
    private var goalTriggered = false

    fun requestJump() {
        jumpRequested = true
    }

    fun resetPlayerToSpawn() {
        val spawn = worldBlocks.firstOrNull { it.type == BlockType.SPAWN }
        playerX = spawn?.x ?: 0f
        playerY = (spawn?.y ?: 0f) + (spawn?.h ?: 1f) * 0.5f + 1.1f
        playerZ = spawn?.z ?: 0f
        playerVelY = 0f
        isGrounded = false
        goalTriggered = false
    }

    fun clearCollectedCoins() {
        collectedCoinIndices.clear()
        goalTriggered = false
    }

    fun orbitBy(dx: Float, dy: Float) {
        autoRotate = false
        cameraYawDeg = (cameraYawDeg - dx * 0.35f) % 360f
        cameraPitchDeg = (cameraPitchDeg + dy * 0.25f).coerceIn(5f, 65f)
    }

    /**
     * Converts a 2D tap in screen pixels into a snapped 3D grid coordinate on the baseplate or top of a hit block.
     */
    fun raycastBuildTarget(touchX: Float, touchY: Float): Triple<Float, Float, Float>? {
        if (viewportWidth <= 0 || viewportHeight <= 0) return null
        val ndcX = (2f * touchX) / viewportWidth - 1f
        val ndcY = 1f - (2f * touchY) / viewportHeight

        val invVp = FloatArray(16)
        if (!Matrix.invertM(invVp, 0, vpMatrix, 0)) return null

        val nearWorld = FloatArray(4)
        val farWorld = FloatArray(4)
        Matrix.multiplyMV(nearWorld, 0, invVp, 0, floatArrayOf(ndcX, ndcY, -1f, 1f), 0)
        Matrix.multiplyMV(farWorld, 0, invVp, 0, floatArrayOf(ndcX, ndcY, 1f, 1f), 0)
        if (nearWorld[3] == 0f || farWorld[3] == 0f) return null

        val ox = nearWorld[0] / nearWorld[3]
        val oy = nearWorld[1] / nearWorld[3]
        val oz = nearWorld[2] / nearWorld[3]

        val fx = farWorld[0] / farWorld[3]
        val fy = farWorld[1] / farWorld[3]
        val fz = farWorld[2] / farWorld[3]

        val dx = fx - ox
        val dy = fy - oy
        val dz = fz - oz
        val len = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(0.0001f)
        val rx = dx / len
        val ry = dy / len
        val rz = dz / len

        // Step ray against existing blocks first
        val blocks = worldBlocks
        var t = 0.5f
        while (t < 38f) {
            val px = ox + rx * t
            val py = oy + ry * t
            val pz = oz + rz * t
            for (b in blocks) {
                val hx = b.w * 0.5f
                val hy = b.h * 0.5f
                val hz = b.d * 0.5f
                if (abs(px - b.x) <= hx && abs(py - b.y) <= hy && abs(pz - b.z) <= hz) {
                    // Place on top of hit block
                    val gx = kotlin.math.round(b.x)
                    val gy = kotlin.math.round((b.y + hy + 0.5f) * 2f) / 2f
                    val gz = kotlin.math.round(b.z)
                    return Triple(gx.coerceIn(-9f, 9f), gy.coerceIn(0.5f, 10f), gz.coerceIn(-9f, 9f))
                }
            }
            // Ground plane y = 0
            if (py <= 0f) {
                val gx = kotlin.math.round(px)
                val gz = kotlin.math.round(pz)
                return Triple(gx.coerceIn(-9f, 9f), 0.5f, gz.coerceIn(-9f, 9f))
            }
            t += 0.25f
        }
        return null
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        mesh.initGL()
        if (mode == RenderMode.PLAY) {
            resetPlayerToSpawn()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = max(1, width)
        viewportHeight = max(1, height)
        GLES20.glViewport(0, 0, viewportWidth, viewportHeight)
        val ratio = viewportWidth.toFloat() / viewportHeight.toFloat()
        Matrix.perspectiveM(projMatrix, 0, 52f, ratio, 0.2f, 120f)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = ((now - lastFrameNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
        lastFrameNanos = now
        elapsedSec += dt

        val theme = skyTheme
        GLES20.glClearColor(theme.skyRed, theme.skyGreen, theme.skyBlue, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (autoRotate && (mode == RenderMode.SHOWCASE || mode == RenderMode.ACCESSORY_PREVIEW)) {
            cameraYawDeg = (cameraYawDeg + dt * 28f) % 360f
        }

        if (mode == RenderMode.PLAY) {
            stepPhysics(dt)
        }

        updateCamera()
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, viewMatrix, 0)

        mesh.beginFrame()

        when (mode) {
            RenderMode.SHOWCASE -> {
                // Pedestal baseplate
                drawWorldBox(0f, -0.35f, 0f, 5.2f, 0.7f, 5.2f, 0xFF2EA043, studded = true)
                drawWorldBox(0f, -0.75f, 0f, 5.6f, 0.3f, 5.6f, 0xFF8C96A4, studded = false)
                // Ground shadow
                drawWorldBox(0f, 0.02f, 0f, 1.3f, 0.02f, 1.1f, 0xFF1A3A1E, studded = false, alpha = 0.35f)
                val wavePhase = elapsedSec * 2.2f
                drawAvatar(
                    appearance = avatarAppearance,
                    x = 0f,
                    y = 0f,
                    z = 0f,
                    yawDeg = 0f,
                    walkPhase = wavePhase,
                    isMoving = false,
                    idleWave = true
                )
            }

            RenderMode.ACCESSORY_PREVIEW -> {
                drawWorldBox(0f, -0.35f, 0f, 4.4f, 0.7f, 4.4f, 0xFF1E6BE6, studded = true)
                drawAvatar(
                    appearance = avatarAppearance,
                    x = 0f,
                    y = 0f,
                    z = 0f,
                    yawDeg = 0f,
                    walkPhase = 0f,
                    isMoving = false,
                    idleWave = false
                )
            }

            RenderMode.PLAY -> {
                // Classic studded baseplate floor
                drawWorldBox(0f, -0.5f, -6f, 36f, 1f, 36f, theme.groundColorHex, studded = true)
                drawWorldBlocks(worldBlocks, hideCollectedCoins = true)

                // Player shadow + avatar
                drawWorldBox(playerX, playerY - 0.92f, playerZ, 1.15f, 0.04f, 0.95f, 0xFF182218, studded = false, alpha = 0.35f)
                val moving = abs(joyX) > 0.08f || abs(joyY) > 0.08f
                drawAvatar(
                    appearance = avatarAppearance,
                    x = playerX,
                    y = playerY - 0.95f,
                    z = playerZ,
                    yawDeg = playerYawDeg,
                    walkPhase = walkPhase,
                    isMoving = moving,
                    idleWave = false
                )

                // Render Friends / Community NPCs in the 3D world + project their nametags
                val projected = mutableListOf<ProjectedLabel>()
                npcAvatars.forEachIndexed { idx, npc ->
                    val bobAngle = elapsedSec * 1.1f + idx * 1.9f
                    val nx = npc.x + sin(bobAngle.toDouble()).toFloat() * 0.9f
                    val nz = npc.z + cos(bobAngle.toDouble()).toFloat() * 0.9f
                    val ny = npc.y
                    val nyaw = Math.toDegrees(bobAngle.toDouble()).toFloat() + 90f
                    drawWorldBox(nx, ny - 0.02f, nz, 1.1f, 0.04f, 0.9f, 0xFF182218, studded = false, alpha = 0.30f)
                    drawAvatar(
                        appearance = npc.appearance,
                        x = nx,
                        y = ny,
                        z = nz,
                        yawDeg = nyaw,
                        walkPhase = elapsedSec * 5f + idx,
                        isMoving = true,
                        idleWave = false
                    )
                    projectToScreen(nx, ny + 2.75f, nz)?.let { (sx, sy) ->
                        projected += ProjectedLabel(
                            username = npc.username,
                            screenX = sx,
                            screenY = sy,
                            isFriend = npc.isFriend,
                            chatLine = npc.chatLine
                        )
                    }
                }
                onProjectedLabels(projected)
            }

            RenderMode.BUILD_WORLD -> {
                // Studio Baseplate (20x20 studded grid)
                drawWorldBox(0f, -0.5f, 0f, 20f, 1f, 20f, theme.groundColorHex, studded = true)
                drawWorldBlocks(worldBlocks, hideCollectedCoins = false)
                buildCursorPos?.let { (cx, cy, cz) ->
                    drawWorldBox(cx, cy, cz, 1.04f, 1.04f, 1.04f, 0xFFF5C518, studded = false, alpha = 0.48f)
                }
            }
        }

        mesh.endFrame()
    }

    private fun updateCamera() {
        val yawRad = Math.toRadians(cameraYawDeg.toDouble())
        val pitchRad = Math.toRadians(cameraPitchDeg.toDouble())
        when (mode) {
            RenderMode.SHOWCASE -> {
                val dist = 5.4f
                val eyeX = (sin(yawRad) * cos(pitchRad) * dist).toFloat()
                val eyeY = (sin(pitchRad) * dist).toFloat() + 1.25f
                val eyeZ = (cos(yawRad) * cos(pitchRad) * dist).toFloat()
                Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, 0f, 1.15f, 0f, 0f, 1f, 0f)
            }
            RenderMode.ACCESSORY_PREVIEW -> {
                val dist = 3.5f
                val eyeX = (sin(yawRad) * cos(pitchRad) * dist).toFloat()
                val eyeY = (sin(pitchRad) * dist).toFloat() + 1.55f
                val eyeZ = (cos(yawRad) * cos(pitchRad) * dist).toFloat()
                Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, 0f, 1.55f, 0f, 0f, 1f, 0f)
            }
            RenderMode.PLAY -> {
                val dist = 7.4f
                val eyeX = playerX + (sin(yawRad) * cos(pitchRad) * dist).toFloat()
                val eyeY = playerY + (sin(pitchRad) * dist).toFloat() + 1.1f
                val eyeZ = playerZ + (cos(yawRad) * cos(pitchRad) * dist).toFloat()
                Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, playerX, playerY + 0.55f, playerZ, 0f, 1f, 0f)
            }
            RenderMode.BUILD_WORLD -> {
                val dist = 16.5f
                val eyeX = (sin(yawRad) * cos(pitchRad) * dist).toFloat()
                val eyeY = (sin(pitchRad) * dist).toFloat() + 2.5f
                val eyeZ = (cos(yawRad) * cos(pitchRad) * dist).toFloat()
                Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, 0f, 1.0f, 0f, 0f, 1f, 0f)
            }
        }
    }

    private fun stepPhysics(dt: Float) {
        val blocks = worldBlocks
        val speed = 6.2f
        val yawRad = Math.toRadians(cameraYawDeg.toDouble())
        // Camera forward on XZ plane is (-sin(yaw), -cos(yaw)); right is (cos(yaw), -sin(yaw))
        val fwdX = -sin(yawRad).toFloat()
        val fwdZ = -cos(yawRad).toFloat()
        val rightX = cos(yawRad).toFloat()
        val rightZ = -sin(yawRad).toFloat()

        // joyY up is negative in screen coords -> forward = -joyY
        val moveForward = -joyY
        val moveRight = joyX
        var vx = (fwdX * moveForward + rightX * moveRight) * speed
        var vz = (fwdZ * moveForward + rightZ * moveRight) * speed
        val mag = sqrt(vx * vx + vz * vz)
        if (mag > 0.25f) {
            playerYawDeg = Math.toDegrees(kotlin.math.atan2(vx.toDouble(), vz.toDouble())).toFloat()
            walkPhase += dt * 11f
        } else {
            vx = 0f
            vz = 0f
        }

        if (jumpRequested) {
            jumpRequested = false
            if (isGrounded) {
                playerVelY = 8.6f
                isGrounded = false
            }
        }

        playerVelY -= 21f * dt

        val halfR = 0.42f
        val halfH = 0.95f

        // Move X with solid collision
        playerX = (playerX + vx * dt).coerceIn(-16.5f, 16.5f)
        for (b in blocks) {
            if (!b.type.isSolid) continue
            if (overlaps(playerX, playerY, playerZ, halfR, halfH, b)) {
                if (vx > 0f) playerX = b.x - b.w * 0.5f - halfR - 0.001f
                else if (vx < 0f) playerX = b.x + b.w * 0.5f + halfR + 0.001f
            }
        }

        // Move Z with solid collision
        playerZ = (playerZ + vz * dt).coerceIn(-23f, 11.5f)
        for (b in blocks) {
            if (!b.type.isSolid) continue
            if (overlaps(playerX, playerY, playerZ, halfR, halfH, b)) {
                if (vz > 0f) playerZ = b.z - b.d * 0.5f - halfR - 0.001f
                else if (vz < 0f) playerZ = b.z + b.d * 0.5f + halfR + 0.001f
            }
        }

        // Move Y with floor / ceiling collision
        playerY += playerVelY * dt
        isGrounded = false

        // Baseplate floor at y = 0
        if (playerY - halfH <= 0f) {
            playerY = halfH
            playerVelY = 0f
            isGrounded = true
        }

        blocks.forEachIndexed { idx, b ->
            when (b.type) {
                BlockType.COIN -> {
                    if (!collectedCoinIndices.contains(idx) && overlaps(playerX, playerY, playerZ, halfR + 0.25f, halfH + 0.25f, b)) {
                        collectedCoinIndices.add(idx)
                        onCoinsCollectedDelta(10)
                    }
                }
                BlockType.LAVA -> {
                    if (overlaps(playerX, playerY, playerZ, halfR, halfH, b)) {
                        resetPlayerToSpawn()
                        onLavaHit()
                    }
                }
                BlockType.GOAL -> {
                    if (overlaps(playerX, playerY, playerZ, halfR + 0.2f, halfH + 0.2f, b)) {
                        if (playerVelY <= 0f && playerY >= b.y) {
                            playerY = b.y + b.h * 0.5f + halfH
                            playerVelY = 0f
                            isGrounded = true
                        }
                        if (!goalTriggered) {
                            goalTriggered = true
                            onGoalReached()
                        }
                    }
                }
                else -> {
                    if (b.type.isSolid && overlaps(playerX, playerY, playerZ, halfR, halfH, b)) {
                        if (playerVelY <= 0f && playerY - halfH <= b.y + b.h * 0.5f) {
                            playerY = b.y + b.h * 0.5f + halfH
                            playerVelY = 0f
                            isGrounded = true
                        } else if (playerVelY > 0f) {
                            playerY = b.y - b.h * 0.5f - halfH - 0.001f
                            playerVelY = 0f
                        }
                    }
                }
            }
        }
    }

    private fun overlaps(px: Float, py: Float, pz: Float, hr: Float, hh: Float, b: WorldBlock): Boolean {
        return abs(px - b.x) < (hr + b.w * 0.5f) &&
            abs(py - b.y) < (hh + b.h * 0.5f) &&
            abs(pz - b.z) < (hr + b.d * 0.5f)
    }

    private fun drawWorldBlocks(blocks: List<WorldBlock>, hideCollectedCoins: Boolean) {
        blocks.forEachIndexed { idx, b ->
            if (b.type == BlockType.COIN) {
                if (hideCollectedCoins && collectedCoinIndices.contains(idx)) return@forEachIndexed
                // Spinning floating coin
                val spin = (elapsedSec * 140f + idx * 45f) % 360f
                val bob = sin((elapsedSec * 3f + idx).toDouble()).toFloat() * 0.12f
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, b.x, b.y + bob, b.z)
                Matrix.rotateM(modelMatrix, 0, spin, 0f, 1f, 0f)
                Matrix.scaleM(modelMatrix, 0, b.w, b.h, b.d)
                Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
                mesh.drawBox(mvpMatrix, modelMatrix, b.colorHex, b.w, b.h, b.d, studded = false)
            } else {
                val studded = b.type == BlockType.GRASS || b.type == BlockType.BRICK ||
                    b.type == BlockType.SPAWN || b.type == BlockType.GOAL || b.type == BlockType.BLUE_BLOCK
                val alpha = if (b.type == BlockType.WATER) 0.85f else 1f
                drawWorldBox(b.x, b.y, b.z, b.w, b.h, b.d, b.colorHex, studded = studded, alpha = alpha)
            }
        }
    }

    private fun drawAvatar(
        appearance: AvatarAppearance,
        x: Float,
        y: Float,
        z: Float,
        yawDeg: Float,
        walkPhase: Float,
        isMoving: Boolean,
        idleWave: Boolean
    ) {
        val swing = if (isMoving) sin(walkPhase.toDouble()).toFloat() * 34f else 0f
        val waveArm = if (idleWave) -145f + sin((walkPhase * 1.4f).toDouble()).toFloat() * 20f else -swing

        // Legs (bottom at y, height 0.72)
        drawLimb(x, y, z, yawDeg, -0.18f, 0.36f, 0f, swing, 0.34f, 0.72f, 0.38f, appearance.leftLegColorHex)
        drawLimb(x, y, z, yawDeg, 0.18f, 0.36f, 0f, -swing, 0.34f, 0.72f, 0.38f, appearance.rightLegColorHex)

        // Torso (y from 0.72 to 1.48)
        drawLimb(x, y, z, yawDeg, 0f, 1.10f, 0f, 0f, 0.72f, 0.76f, 0.42f, appearance.torsoColorHex)

        // Arms
        drawLimb(x, y, z, yawDeg, -0.54f, 1.10f, 0f, -swing, 0.32f, 0.74f, 0.38f, appearance.leftArmColorHex)
        drawLimb(x, y, z, yawDeg, 0.54f, 1.10f, 0f, waveArm, 0.32f, 0.74f, 0.38f, appearance.rightArmColorHex)

        // Head (center at y + 1.78)
        drawLimb(x, y, z, yawDeg, 0f, 1.78f, 0f, 0f, 0.60f, 0.58f, 0.60f, appearance.headColorHex)

        // Classic friendly face eyes & smile blocks on +Z face of head
        drawLimb(x, y, z, yawDeg, -0.12f, 1.83f, 0.305f, 0f, 0.08f, 0.10f, 0.03f, 0xFF1A2230)
        drawLimb(x, y, z, yawDeg, 0.12f, 1.83f, 0.305f, 0f, 0.08f, 0.10f, 0.03f, 0xFF1A2230)
        drawLimb(x, y, z, yawDeg, 0f, 1.69f, 0.305f, 0f, 0.22f, 0.055f, 0.03f, 0xFF1A2230)

        // 3D Equipped Hat & Face Accessories (anchored to head center y + 1.78)
        drawAccessoryParts(appearance.hatParts, x, y + 1.78f, z, yawDeg)
        drawAccessoryParts(appearance.faceParts, x, y + 1.78f, z, yawDeg)
        // 3D Equipped Back Accessories (anchored to torso center y + 1.10)
        drawAccessoryParts(appearance.backParts, x, y + 1.10f, z, yawDeg)
    }

    private fun drawAccessoryParts(
        parts: List<AccessoryPart>,
        ax: Float,
        ay: Float,
        az: Float,
        yawDeg: Float
    ) {
        for (p in parts) {
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, ax, ay, az)
            Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
            Matrix.translateM(modelMatrix, 0, p.ox, p.oy, p.oz)
            Matrix.scaleM(modelMatrix, 0, p.sx, p.sy, p.sz)
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
            mesh.drawBox(mvpMatrix, modelMatrix, p.colorHex, p.sx, p.sy, p.sz, studded = false)
        }
    }

    private fun drawLimb(
        ax: Float,
        ay: Float,
        az: Float,
        yawDeg: Float,
        lx: Float,
        ly: Float,
        lz: Float,
        swingPitchDeg: Float,
        sx: Float,
        sy: Float,
        sz: Float,
        colorHex: Long
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, ax, ay, az)
        Matrix.rotateM(modelMatrix, 0, yawDeg, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, lx, ly, lz)
        if (swingPitchDeg != 0f) {
            Matrix.translateM(modelMatrix, 0, 0f, sy * 0.45f, 0f)
            Matrix.rotateM(modelMatrix, 0, swingPitchDeg, 1f, 0f, 0f)
            Matrix.translateM(modelMatrix, 0, 0f, -sy * 0.45f, 0f)
        }
        Matrix.scaleM(modelMatrix, 0, sx, sy, sz)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        mesh.drawBox(mvpMatrix, modelMatrix, colorHex, sx, sy, sz, studded = false)
    }

    private fun drawWorldBox(
        x: Float,
        y: Float,
        z: Float,
        sx: Float,
        sy: Float,
        sz: Float,
        colorHex: Long,
        studded: Boolean,
        alpha: Float = 1f
    ) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, x, y, z)
        Matrix.scaleM(modelMatrix, 0, sx, sy, sz)
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0)
        mesh.drawBox(mvpMatrix, modelMatrix, colorHex, sx, sy, sz, studded, alpha)
    }

    private fun projectToScreen(wx: Float, wy: Float, wz: Float): Pair<Float, Float>? {
        val worldVec = floatArrayOf(wx, wy, wz, 1f)
        Matrix.multiplyMV(tmpVec, 0, vpMatrix, 0, worldVec, 0)
        val w = tmpVec[3]
        if (w <= 0.1f) return null
        val ndcX = tmpVec[0] / w
        val ndcY = tmpVec[1] / w
        if (ndcX < -1.1f || ndcX > 1.1f || ndcY < -1.1f || ndcY > 1.1f) return null
        val sx = (ndcX + 1f) * 0.5f * viewportWidth
        val sy = (1f - ndcY) * 0.5f * viewportHeight
        return sx to sy
    }
}
