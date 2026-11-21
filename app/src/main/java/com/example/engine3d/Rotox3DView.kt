package com.example.engine3d

import android.annotation.SuppressLint
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.AvatarAppearance
import com.example.data.NpcAvatarState
import com.example.data.SkyTheme
import com.example.data.WorldBlock
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
@Composable
fun Rotox3DView(
    mode: RenderMode,
    modifier: Modifier = Modifier,
    avatarAppearance: AvatarAppearance = AvatarAppearance(),
    skyTheme: SkyTheme = SkyTheme.DAY,
    worldBlocks: List<WorldBlock> = emptyList(),
    npcAvatars: List<NpcAvatarState> = emptyList(),
    joyX: Float = 0f,
    joyY: Float = 0f,
    jumpSignal: Int = 0,
    resetSignal: Int = 0,
    buildCursorPos: Triple<Float, Float, Float>? = null,
    onBuildTap: ((Triple<Float, Float, Float>) -> Unit)? = null,
    onCoinsCollectedDelta: (Int) -> Unit = {},
    onGoalReached: () -> Unit = {},
    onLavaHit: () -> Unit = {},
    onProjectedLabels: (List<ProjectedLabel>) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val renderer = remember(mode) {
        Rotox3DRenderer(
            mode = mode,
            onCoinsCollectedDelta = onCoinsCollectedDelta,
            onGoalReached = onGoalReached,
            onLavaHit = onLavaHit,
            onProjectedLabels = onProjectedLabels
        )
    }

    val glView = remember(renderer) {
        GLSurfaceView(context).apply {
            setEGLContextClientVersion(2)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

            var downX = 0f
            var downY = 0f
            var lastX = 0f
            var lastY = 0f
            var movedDist = 0f

            setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                        lastX = event.x
                        lastY = event.y
                        movedDist = 0f
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        movedDist += abs(dx) + abs(dy)
                        lastX = event.x
                        lastY = event.y
                        renderer.orbitBy(dx, dy)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mode == RenderMode.BUILD_WORLD && movedDist < 18f && onBuildTap != null) {
                            val tx = event.x
                            val ty = event.y
                            queueEvent {
                                renderer.raycastBuildTarget(tx, ty)?.let { hit ->
                                    post { onBuildTap(hit) }
                                }
                            }
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }

    LaunchedEffect(avatarAppearance) {
        renderer.avatarAppearance = avatarAppearance
    }
    LaunchedEffect(skyTheme) {
        renderer.skyTheme = skyTheme
    }
    LaunchedEffect(worldBlocks) {
        renderer.worldBlocks = worldBlocks
    }
    LaunchedEffect(npcAvatars) {
        renderer.npcAvatars = npcAvatars
    }
    LaunchedEffect(joyX, joyY) {
        renderer.joyX = joyX
        renderer.joyY = joyY
    }
    LaunchedEffect(buildCursorPos) {
        renderer.buildCursorPos = buildCursorPos
    }
    LaunchedEffect(jumpSignal) {
        if (jumpSignal > 0) renderer.requestJump()
    }
    LaunchedEffect(resetSignal) {
        if (resetSignal > 0) {
            glView.queueEvent {
                renderer.clearCollectedCoins()
                renderer.resetPlayerToSpawn()
            }
        }
    }

    DisposableEffect(lifecycleOwner, glView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> glView.onResume()
                Lifecycle.Event.ON_PAUSE -> glView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            glView.onPause()
        }
    }

    AndroidView(
        factory = { glView },
        modifier = modifier
    )
}
