package com.example.fitlife

import android.content.Intent
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
import androidx.cardview.widget.CardView
import com.google.android.filament.EntityManager
import com.google.android.filament.LightManager
import com.google.android.filament.Skybox
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.nio.ByteBuffer

class AvatarActivity : AppCompatActivity() {

    private lateinit var surfaceView: SurfaceView
    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer
    private var userId: Int = -1

    // Initial rotation to 0f to face the front
    private var manualRotationY = 0f
    private var lastX = 0f

    private val baseTransform = FloatArray(16).apply {
        android.opengl.Matrix.setIdentityM(this, 0)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            choreographer.postFrameCallback(this)

            // Update model position every frame to fight internal resets
            updateAvatarTransform()

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

        userId = intent.getIntExtra("USER_ID", 1)

        findViewById<View>(R.id.avatarPlaceholder).visibility = View.GONE

        surfaceView = SurfaceView(this).apply {
            setZOrderMediaOverlay(true)
        }
        val container = findViewById<FrameLayout>(R.id.filamentContainer)
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
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastX
                    lastX = event.x
                    manualRotationY += dx * 0.5f
                    updateAvatarTransform()
                }
            }
            true
        }

        // Navigate to Profile on Info Card click
        findViewById<CardView>(R.id.cardAvatarInfo).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btnBackAvatar).setOnClickListener { finish() }

        loadAvatarData(userId)
    }

    private fun updateAvatarTransform() {
        val asset = modelViewer.asset ?: return
        val tm = modelViewer.engine.transformManager
        val instance = tm.getInstance(asset.root)
        if (instance == 0) return

        val rotation = FloatArray(16)
        android.opengl.Matrix.setIdentityM(rotation, 0)
        android.opengl.Matrix.rotateM(rotation, 0, manualRotationY, 0f, 1f, 0f)

        // Aggressive Model-Side Correction
        val correction = FloatArray(16)
        android.opengl.Matrix.setIdentityM(correction, 0)

        // 1. Zoom out and lower the model.
        android.opengl.Matrix.translateM(correction, 0, 5.0f, -60.0f, -170.0f)

        // 2. Scale it to a consistent size
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

        val fillLight = EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(60000.0f)
            .direction(0.5f, 0.5f, 0.5f)
            .build(engine, fillLight)
        scene.addEntity(fillLight)
    }

    private fun loadAvatarData(userId: Int) {
        RetrofitClient.instance.getUser(userId).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                val user = response.body()?.user ?: return
                runOnUiThread {
                    findViewById<TextView>(R.id.tvAvatarName).text = user.username
                    val state = user.fitness_state ?: "healthy"
                    findViewById<TextView>(R.id.tvAvatarState).text = state.uppercase()
                    val gender = user.gender ?: "female"
                    loadModel("${gender}_${state}.glb")
                }
            }
            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("AVATAR_DEBUG", "Server Fail: ${t.message}")
            }
        })
    }

    private fun loadModel(assetName: String) {
        try {
            val buffer = readAsset(assetName)
            modelViewer.loadModelGlb(buffer)
            modelViewer.transformToUnitCube()

            val asset = modelViewer.asset
            if (asset != null) {
                val tm = modelViewer.engine.transformManager
                val instance = tm.getInstance(asset.root)
                if (instance != 0) {
                    tm.getTransform(instance, baseTransform)
                }
                // Face forward initially
                manualRotationY = 0f
                updateAvatarTransform()
            }

            surfaceView.post {
                setupCamera()
                modelViewer.render(System.nanoTime())
            }
        } catch (e: Exception) {
            Log.e("AVATAR_DEBUG", "Failed to load model: $assetName", e)
        }
    }

    private fun setupCamera() {
        val width = surfaceView.width
        val height = surfaceView.height
        if (width <= 0 || height <= 0) return

        val aspect = width.toDouble() / height.toDouble()
        modelViewer.camera.setProjection(45.0, aspect, 0.1, 1000.0, com.google.android.filament.Camera.Fov.VERTICAL)

        // Keep a neutral camera target
        modelViewer.camera.lookAt(
            0.0, 0.5, 4.0,
            0.0, 0.5, 0.0,
            0.0, 1.0, 0.0
        )
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = assets.open(assetName)
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