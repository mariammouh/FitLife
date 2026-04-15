package com.example.fitlife

import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.Skybox
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

class WorkoutActivity : AppCompatActivity() {

    private lateinit var surfaceView: SurfaceView
    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer

    private var manualRotationY = 0f
    private var lastX = 0f

    private val baseTransform = FloatArray(16).apply {
        android.opengl.Matrix.setIdentityM(this, 0)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)
            updateModelTransform()

            modelViewer.animator?.let { animator ->
                if (animator.animationCount > 0) {
                    val seconds = frameTimeNanos / 1_000_000_000.0f
                    val duration = animator.getAnimationDuration(0)
                    val time = seconds % duration
                    animator.applyAnimation(0, time)
                    animator.updateBoneMatrices()
                }
            }

            setupCamera()
            modelViewer.render(frameTimeNanos)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.init()
        setContentView(R.layout.activity_avatar)

        val workoutName = intent.getStringExtra("WORKOUT_NAME") ?: "Yoga"
        Log.d("WORKOUT_DEBUG", "Starting WorkoutActivity for: $workoutName")

        // Find and update the header/title
        val tvHeader = findViewById<TextView>(R.id.tvAvatarHeader)
        tvHeader.text = "Workout Instructor"

        val tvTitle = findViewById<TextView>(R.id.tvAvatarName)
        tvTitle.text = workoutName.uppercase()

        // Update description for context
        val tvDesc = findViewById<TextView>(R.id.tvAvatarDesc)
        tvDesc.text = "Follow the movements of your $workoutName instructor."

        // Hide profile-specific UI
        findViewById<View>(R.id.tvAvatarState)?.visibility = View.GONE
        findViewById<View>(R.id.avatarPlaceholder)?.visibility = View.GONE

        val container = findViewById<FrameLayout>(R.id.filamentContainer)
        surfaceView = SurfaceView(this).apply {
            setZOrderMediaOverlay(true)
        }
        container.removeAllViews()
        container.addView(surfaceView)

        choreographer = Choreographer.getInstance()
        modelViewer = ModelViewer(surfaceView)

        val engine = modelViewer.engine
        val scene = modelViewer.scene
        val skybox = Skybox.Builder()
            .color(0.06f, 0.06f, 0.1f, 1.0f)
            .build(engine)
        scene.skybox = skybox

        setupLighting()

        surfaceView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> lastX = event.x
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastX
                    lastX = event.x
                    manualRotationY += dx * 0.5f
                    updateModelTransform()
                }
            }
            true
        }

        findViewById<ImageButton>(R.id.btnBackAvatar).setOnClickListener { finish() }

        // Attempt to load the model.
        loadModel("workout/${workoutName.lowercase()}.glb")
    }

    private fun updateModelTransform() {
        val asset = modelViewer.asset ?: return
        val tm = modelViewer.engine.transformManager
        val instance = tm.getInstance(asset.root)
        if (instance == 0) return

        val rotation = FloatArray(16)
        android.opengl.Matrix.setIdentityM(rotation, 0)
        android.opengl.Matrix.rotateM(rotation, 0, manualRotationY, 0f, 1f, 0f)

        // Aggressive Model-Side Correction matching AvatarActivity logic
        val correction = FloatArray(16)
        android.opengl.Matrix.setIdentityM(correction, 0)

        // Match AvatarActivity's coordinate system and scale
        android.opengl.Matrix.translateM(correction, 0, 5.0f, -60.0f, -170.0f)
        android.opengl.Matrix.scaleM(correction, 0, 0.6f, 0.6f, 0.6f)

        val temp = FloatArray(16)
        android.opengl.Matrix.multiplyMM(temp, 0, correction, 0, rotation, 0)

        val result = FloatArray(16)
        android.opengl.Matrix.multiplyMM(result, 0, temp, 0, baseTransform, 0)

        tm.setTransform(instance, result)
    }

    private fun setupLighting() {
        val engine = modelViewer.engine
        val scene = modelViewer.scene

        val light = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(120000.0f)
            .direction(0.0f, -1.0f, -1.0f)
            .castShadows(true)
            .build(engine, light)
        scene.addEntity(light)
    }

    private fun loadModel(assetPath: String) {
        try {
            Log.d("WORKOUT_DEBUG", "Loading asset: $assetPath")
            val buffer = readAsset(assetPath)
            modelViewer.loadModelGlb(buffer)
            modelViewer.transformToUnitCube()

            val asset = modelViewer.asset
            if (asset != null) {
                val tm = modelViewer.engine.transformManager
                val instance = tm.getInstance(asset.root)
                if (instance != 0) {
                    tm.getTransform(instance, baseTransform)
                }
                manualRotationY = 0f
                updateModelTransform()
            }

            surfaceView.post {
                setupCamera()
                modelViewer.render(System.nanoTime())
            }
        } catch (e: Exception) {
            Log.e("WORKOUT_DEBUG", "Failed to load: $assetPath", e)
        }
    }

    private fun setupCamera() {
        val width = surfaceView.width
        val height = surfaceView.height
        if (width <= 0 || height <= 0) return

        val aspect = width.toDouble() / height.toDouble()
        modelViewer.camera.setProjection(45.0, aspect, 0.1, 1000.0, com.google.android.filament.Camera.Fov.VERTICAL)
        modelViewer.camera.lookAt(0.0, 0.5, 4.0, 0.0, 0.5, 0.0, 0.0, 1.0, 0.0)
    }

    private fun readAsset(assetPath: String): ByteBuffer {
        val input = assets.open(assetPath)
        val bytes = input.readBytes()
        val buffer = ByteBuffer.allocateDirect(bytes.size)
        buffer.put(bytes)
        buffer.rewind()
        return buffer
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
    }
}