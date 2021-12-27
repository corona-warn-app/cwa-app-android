package de.rki.coronawarnapp.qrcode.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.window.WindowManager
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.BuildVersionWrap
import de.rki.coronawarnapp.util.lessThanAPILevel
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class QrCodeScannerPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    private val cameraPreview: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val windowManager: WindowManager

    var scanEnabled = true

    init {
        inflate(context, R.layout.qr_code_scanner_preview_view, this)
        cameraPreview = findViewById(R.id.camera_preview)
        windowManager = WindowManager(context)
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
            },
            ContextCompat.getMainExecutor(context)
        )

        setupAutofocus(lifecycleOwner)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.tag(TAG).d("Shutting down camera executor")
        cameraExecutor.shutdown()
    }

    private fun setupAutofocus(lifecycleOwner: LifecycleOwner) {
        if (BuildVersionWrap.lessThanAPILevel(Build.VERSION_CODES.O)) {
            lifecycleOwner.lifecycleScope.launchWhenStarted {
                while (true) {
                    runCatching { autoFocus() }.onFailure { Timber.tag(TAG).e(it, "setupAutofocus") }
                    delay(1_000)
                }
            }
        }
    }

    private fun bindUseCases(lifecycleOwner: LifecycleOwner, onImageCallback: (ImageProxy) -> Unit) {
        Timber.tag(TAG).d("Binding camera use cases")
        // Get screen metrics used to setup camera for full screen resolution
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

    private suspend fun autoFocus() {
        tapToFocus()

        val bounds = windowManager.getCurrentWindowMetrics().bounds
        val focusPoint = SurfaceOrientedMeteringPointFactory(
            bounds.width().toFloat(),
            bounds.height().toFloat()
        ).createPoint(
            bounds.exactCenterX(),
            bounds.exactCenterY()
        )

        val focusAction = FocusMeteringAction.Builder(focusPoint, FocusMeteringAction.FLAG_AF).build()
        camera?.cameraControl?.startFocusAndMetering(focusAction)?.let { future ->
            suspendCoroutine<Unit> { continuation ->
                future.addListener(
                    { continuation.resume(Unit) },
                    ContextCompat.getMainExecutor(context),
                )
            }
        }
    }

    private fun tapToFocus() {
        cameraPreview.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> true
                MotionEvent.ACTION_UP -> {
                    val focusPoint = SurfaceOrientedMeteringPointFactory(view.width.toFloat(), view.height.toFloat())
                        .createPoint(event.x, event.y)

                    camera?.cameraControl?.startFocusAndMetering(
                        FocusMeteringAction.Builder(focusPoint, FocusMeteringAction.FLAG_AF)
                            .disableAutoCancel()
                            .build()
                    )
                    view.performClick()
                }
                else -> false
            }
        }
    }

    companion object {
        private val TAG = tag<QrCodeScannerPreviewView>()

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}
