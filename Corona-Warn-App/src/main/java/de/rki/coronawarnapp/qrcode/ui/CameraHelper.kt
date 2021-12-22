package de.rki.coronawarnapp.qrcode.ui

import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.window.WindowManager
import de.rki.coronawarnapp.tag
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraHelper(
    lifecycleOwner: LifecycleOwner,
    cameraPreview: PreviewView,
    private val onImageCallback: (ImageProxy) -> Unit
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private val hasBackCamera: Boolean
        get() = hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

    private val hasFrontCamera: Boolean
        get() = hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)

    var scanEnabled = true

    fun enableTorch(enable: Boolean) {
        camera?.cameraControl?.enableTorch(enable)
    }

    private val onDestroyListener = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)

            Timber.tag(TAG).d("Shutting down camera executor")
            cameraExecutor.shutdown()
            owner.lifecycle.removeObserver(this)
        }
    }

    init {
        setupCamera(cameraPreview, lifecycleOwner)
        addOnDestroyListener(lifecycleOwner)
    }

    private fun setupCamera(cameraPreviewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        Timber.tag(TAG).d("Setting up camera")
        val context = cameraPreviewView.context
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindUseCases(cameraPreviewView, lifecycleOwner)
            }, ContextCompat.getMainExecutor(context)
        )
    }

    private fun bindUseCases(cameraPreview: PreviewView, lifecycleOwner: LifecycleOwner) {
        Timber.tag(TAG).d("Binding camera use cases")
        // Get screen metrics used to setup camera for full screen resolution
        val windowManager = WindowManager(cameraPreview.context)
        val metrics = windowManager.getCurrentWindowMetrics().bounds
        Timber.tag(TAG).d("Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Timber.tag(TAG).d("Preview aspect ratio: $screenAspectRatio")

        val rotation = cameraPreview.display.rotation

        val preview = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also { it.setSurfaceProvider(cameraPreview.surfaceProvider) }

        val lensFacing = when {
            hasBackCamera -> CameraSelector.LENS_FACING_BACK
            hasFrontCamera -> CameraSelector.LENS_FACING_FRONT
            else -> throw IllegalStateException("Back and front camera are unavailable")
        }
        val selector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val analyzer = ImageAnalysis.Builder()
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(cameraExecutor) {
                    onImage(imageProxy = it)
                }
            }

        try {
            val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera not initialized")
            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analyzer)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }
    }

    private fun onImage(imageProxy: ImageProxy) {
        if (scanEnabled) {
            onImageCallback(imageProxy)
        } else {
            imageProxy.use {  }
        }
    }

    private fun addOnDestroyListener(lifecycleOwner: LifecycleOwner) {
        Timber.tag(TAG).d("Adding on destroy listener to lifecycle owner")
        lifecycleOwner.lifecycle.addObserver(onDestroyListener)
    }

    private fun hasCamera(selector: CameraSelector) = cameraProvider?.hasCamera(selector) ?: false

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {
        private val TAG = tag<CameraHelper>()

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}
