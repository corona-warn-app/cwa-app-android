package de.rki.coronawarnapp.ui.qrcode

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.window.WindowManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tag
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class QrCodeScannerPreviewView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    private val cameraPreview: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    var scanEnabled = true

    init {
        inflate(context, R.layout.qr_code_scanner_preview_view, this)
        cameraPreview = findViewById(R.id.camera_preview)
    }

    fun enableTorch(enable: Boolean) {
        camera?.cameraControl?.enableTorch(enable)
    }

    fun setupCamera(lifecycleOwner: LifecycleOwner, onImageCallback: (ImageProxy) -> Unit) {
        Timber.tag(TAG).d("Setting up camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindUseCases(lifecycleOwner, onImageCallback)
            }, ContextCompat.getMainExecutor(context)
        )
    }

    private fun bindUseCases(lifecycleOwner: LifecycleOwner, onImageCallback: (ImageProxy) -> Unit) {
        Timber.tag(TAG).d("Binding camera use cases")
        // Get screen metrics used to setup camera for full screen resolution
        val windowManager = WindowManager(context)
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
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(rotation)
            .build()
            .also { imageAnalysis ->
                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    if (scanEnabled) {
                        onImageCallback(imageProxy)
                    } else {
                        // Close image safely
                        imageProxy.use { }
                    }
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

    private val hasBackCamera: Boolean
        get() = hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)

    private val hasFrontCamera: Boolean
        get() = hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)

    private fun hasCamera(selector: CameraSelector) = cameraProvider?.hasCamera(selector) ?: false

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.tag(TAG).d("Shutting down camera executor")
        cameraExecutor.shutdown()
    }

    companion object {
        private val TAG = tag<QrCodeScannerPreviewView>()

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}
