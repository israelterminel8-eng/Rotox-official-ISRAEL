package com.example.engine3d

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class CubeMesh {
    private var programId = 0
    private var aPosition = 0
    private var aNormal = 0
    private var aTexCoord = 0
    private var uMvpMatrix = 0
    private var uModelMatrix = 0
    private var uColor = 0
    private var uScale = 0
    private var uIsStudded = 0
    private var uAlpha = 0

    private val vertexBuffer: FloatBuffer

    // 36 vertices: x, y, z, nx, ny, nz, u, v
    private val cubeVertices = floatArrayOf(
        // Front (+Z)
        -0.5f, -0.5f,  0.5f,  0f,  0f,  1f, 0f, 0f,
         0.5f, -0.5f,  0.5f,  0f,  0f,  1f, 1f, 0f,
         0.5f,  0.5f,  0.5f,  0f,  0f,  1f, 1f, 1f,
        -0.5f, -0.5f,  0.5f,  0f,  0f,  1f, 0f, 0f,
         0.5f,  0.5f,  0.5f,  0f,  0f,  1f, 1f, 1f,
        -0.5f,  0.5f,  0.5f,  0f,  0f,  1f, 0f, 1f,

        // Back (-Z)
         0.5f, -0.5f, -0.5f,  0f,  0f, -1f, 0f, 0f,
        -0.5f, -0.5f, -0.5f,  0f,  0f, -1f, 1f, 0f,
        -0.5f,  0.5f, -0.5f,  0f,  0f, -1f, 1f, 1f,
         0.5f, -0.5f, -0.5f,  0f,  0f, -1f, 0f, 0f,
        -0.5f,  0.5f, -0.5f,  0f,  0f, -1f, 1f, 1f,
         0.5f,  0.5f, -0.5f,  0f,  0f, -1f, 0f, 1f,

        // Left (-X)
        -0.5f, -0.5f, -0.5f, -1f,  0f,  0f, 0f, 0f,
        -0.5f, -0.5f,  0.5f, -1f,  0f,  0f, 1f, 0f,
        -0.5f,  0.5f,  0.5f, -1f,  0f,  0f, 1f, 1f,
        -0.5f, -0.5f, -0.5f, -1f,  0f,  0f, 0f, 0f,
        -0.5f,  0.5f,  0.5f, -1f,  0f,  0f, 1f, 1f,
        -0.5f,  0.5f, -0.5f, -1f,  0f,  0f, 0f, 1f,

        // Right (+X)
         0.5f, -0.5f,  0.5f,  1f,  0f,  0f, 0f, 0f,
         0.5f, -0.5f, -0.5f,  1f,  0f,  0f, 1f, 0f,
         0.5f,  0.5f, -0.5f,  1f,  0f,  0f, 1f, 1f,
         0.5f, -0.5f,  0.5f,  1f,  0f,  0f, 0f, 0f,
         0.5f,  0.5f, -0.5f,  1f,  0f,  0f, 1f, 1f,
         0.5f,  0.5f,  0.5f,  1f,  0f,  0f, 0f, 1f,

        // Top (+Y)
        -0.5f,  0.5f,  0.5f,  0f,  1f,  0f, 0f, 0f,
         0.5f,  0.5f,  0.5f,  0f,  1f,  0f, 1f, 0f,
         0.5f,  0.5f, -0.5f,  0f,  1f,  0f, 1f, 1f,
        -0.5f,  0.5f,  0.5f,  0f,  1f,  0f, 0f, 0f,
         0.5f,  0.5f, -0.5f,  0f,  1f,  0f, 1f, 1f,
        -0.5f,  0.5f, -0.5f,  0f,  1f,  0f, 0f, 1f,

        // Bottom (-Y)
        -0.5f, -0.5f, -0.5f,  0f, -1f,  0f, 0f, 0f,
         0.5f, -0.5f, -0.5f,  0f, -1f,  0f, 1f, 0f,
         0.5f, -0.5f,  0.5f,  0f, -1f,  0f, 1f, 1f,
        -0.5f, -0.5f, -0.5f,  0f, -1f,  0f, 0f, 0f,
         0.5f, -0.5f,  0.5f,  0f, -1f,  0f, 1f, 1f,
        -0.5f, -0.5f,  0.5f,  0f, -1f,  0f, 0f, 1f
    )

    init {
        vertexBuffer = ByteBuffer.allocateDirect(cubeVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(cubeVertices)
                position(0)
            }
    }

    fun initGL() {
        val vsCode = """
            uniform mat4 uMvpMatrix;
            uniform mat4 uModelMatrix;
            attribute vec3 aPosition;
            attribute vec3 aNormal;
            attribute vec2 aTexCoord;
            varying vec3 vNormal;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = uMvpMatrix * vec4(aPosition, 1.0);
                vNormal = normalize(mat3(uModelMatrix) * aNormal);
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        val fsCode = """
            precision mediump float;
            uniform vec3 uColor;
            uniform vec3 uScale;
            uniform float uIsStudded;
            uniform float uAlpha;
            varying vec3 vNormal;
            varying vec2 vTexCoord;
            void main() {
                vec3 sunDir = normalize(vec3(0.45, 0.85, 0.55));
                float diff = max(dot(normalize(vNormal), sunDir), 0.0);
                float hemi = 0.52 + 0.14 * vNormal.y;
                float lighting = min(1.08, hemi + 0.48 * diff);

                vec3 finalColor = uColor * lighting;

                // Subtle classic plastic stud rings on top face when enabled
                if (uIsStudded > 0.5 && vNormal.y > 0.85) {
                    vec2 worldUv = vec2(vTexCoord.x * max(1.0, uScale.x), vTexCoord.y * max(1.0, uScale.z));
                    vec2 cell = fract(worldUv) - vec2(0.5, 0.5);
                    float dist = length(cell);
                    if (dist < 0.25 && dist > 0.19) {
                        finalColor *= 0.86;
                    } else if (dist <= 0.19) {
                        finalColor *= 1.08;
                    }
                }

                // Crisp classic brick edge bevel
                float edgeX = min(vTexCoord.x, 1.0 - vTexCoord.x);
                float edgeY = min(vTexCoord.y, 1.0 - vTexCoord.y);
                if (min(edgeX, edgeY) < 0.025) {
                    finalColor *= 0.90;
                }

                gl_FragColor = vec4(clamp(finalColor, 0.0, 1.0), uAlpha);
            }
        """.trimIndent()

        val vs = compileShader(GLES20.GL_VERTEX_SHADER, vsCode)
        val fs = compileShader(GLES20.GL_FRAGMENT_SHADER, fsCode)
        programId = GLES20.glCreateProgram().also { prog ->
            GLES20.glAttachShader(prog, vs)
            GLES20.glAttachShader(prog, fs)
            GLES20.glLinkProgram(prog)
        }

        aPosition = GLES20.glGetAttribLocation(programId, "aPosition")
        aNormal = GLES20.glGetAttribLocation(programId, "aNormal")
        aTexCoord = GLES20.glGetAttribLocation(programId, "aTexCoord")
        uMvpMatrix = GLES20.glGetUniformLocation(programId, "uMvpMatrix")
        uModelMatrix = GLES20.glGetUniformLocation(programId, "uModelMatrix")
        uColor = GLES20.glGetUniformLocation(programId, "uColor")
        uScale = GLES20.glGetUniformLocation(programId, "uScale")
        uIsStudded = GLES20.glGetUniformLocation(programId, "uIsStudded")
        uAlpha = GLES20.glGetUniformLocation(programId, "uAlpha")
    }

    fun beginFrame() {
        GLES20.glUseProgram(programId)
        val stride = 8 * 4

        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(aPosition)
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer)

        vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(aNormal)
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, stride, vertexBuffer)

        vertexBuffer.position(6)
        GLES20.glEnableVertexAttribArray(aTexCoord)
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, stride, vertexBuffer)
    }

    fun drawBox(
        mvpMatrix: FloatArray,
        modelMatrix: FloatArray,
        colorHex: Long,
        sx: Float,
        sy: Float,
        sz: Float,
        studded: Boolean,
        alpha: Float = 1f
    ) {
        val r = ((colorHex shr 16) and 0xFF) / 255f
        val g = ((colorHex shr 8) and 0xFF) / 255f
        val b = (colorHex and 0xFF) / 255f

        GLES20.glUniformMatrix4fv(uMvpMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrix, 1, false, modelMatrix, 0)
        GLES20.glUniform3f(uColor, r, g, b)
        GLES20.glUniform3f(uScale, sx, sy, sz)
        GLES20.glUniform1f(uIsStudded, if (studded) 1f else 0f)
        GLES20.glUniform1f(uAlpha, alpha)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36)
    }

    fun endFrame() {
        GLES20.glDisableVertexAttribArray(aPosition)
        GLES20.glDisableVertexAttribArray(aNormal)
        GLES20.glDisableVertexAttribArray(aTexCoord)
    }

    private fun compileShader(type: Int, code: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, code)
        GLES20.glCompileShader(shader)
        return shader
    }
}
