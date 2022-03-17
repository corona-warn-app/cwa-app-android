package de.rki.coronawarnapp.qrcode.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Surface
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
import de.rki.coronawarnapp.qrcode.parser.QrCodeBoofCVParser
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
    private val cameraExecutor by lazy { Executors.newSingleThreadExecutor() }
    private val windowManager: WindowManager
    private val qrCodeBoofCVParser by lazy { QrCodeBoofCVParser() }
    private var parseResultCallback: ParseResultCallback? = null

    init {
        inflate(context, R.layout.qr_code_scanner_preview_view, this)
        cameraPreview = findViewById(R.id.camera_preview)
        windowManager = WindowManager(context)
        cameraPreview.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    }

    fun enableTorch(enable: Boolean) {
        camera?.cameraControl?.enableTorch(enable)
    }

    fun setupCamera(lifecycleOwner: LifecycleOwner) {
        Timber.tag(TAG).d("Setting up camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindUseCases(lifecycleOwner)
            },
            ContextCompat.getMainExecutor(context)
        )

        setupAutofocus(lifecycleOwner)
    }

    fun decodeSingle(parseResultCallback: ParseResultCallback) {
        this.parseResultCallback = parseResultCallback
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Timber.tag(TAG).d("Shutting down camera executor")
        cameraExecutor.shutdown()
    }

    private fun setupAutofocus(lifecycleOwner: LifecycleOwner) {
        if (BuildVersionWrap.lessThanAPILevel(Build.VERSION_CODES.O)) {
            Timber.tag(TAG).d("setupAutofocus()")
            lifecycleOwner.lifecycleScope.launchWhenStarted {
                while (true) {
                    runCatching { autoFocus() }.onFailure { Timber.tag(TAG).e(it, "setupAutofocus failed") }
                    delay(1_000)
                }
            }
        } else {
            Timber.tag(TAG).d("setupAutofocus isn't required")
        }
    }

    private fun bindUseCases(lifecycleOwner: LifecycleOwner) {
        Timber.tag(TAG).d("Binding camera use cases")
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = windowManager.getCurrentWindowMetrics().bounds
        Timber.tag(TAG).d("Screen metrics: %d x %d", metrics.width(), metrics.height())

        val screenAspectRatio = aspectRatio(metrics.width(), metrics.height())
        Timber.tag(TAG).d("Preview aspect ratio: %d", screenAspectRatio)

        val rotation = cameraPreview.display?.rotation ?: Surface.ROTATION_0
        Timber.tag(TAG).d("Preview orientation: %d", rotation)

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
                    handleImage(imageProxy = imageProxy)
                }
            }

        try {
            val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera not initialized")
            cameraProvider.unbindAll()

            Timber.tag(TAG).d("Binding use cases")
            camera = cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, analyzer)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }
    }

    private fun handleImage(imageProxy: ImageProxy) = imageProxy.use {
        if (parseResultCallback == null) return@use
        when (val parseResult = qrCodeBoofCVParser.parseQrCode(it)) {
            is QrCodeBoofCVParser.ParseResult.Failure -> sendResult(parseResult)
            is QrCodeBoofCVParser.ParseResult.Success -> if (parseResult.isNotEmpty) {
                sendResult(parseResult)
            }
        }
    }

    private fun sendResult(result: QrCodeBoofCVParser.ParseResult) {
        parseResultCallback?.invoke(result)
        parseResultCallback = null
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

    companion object {
        private val TAG = tag<QrCodeScannerPreviewView>()

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}

private typealias ParseResultCallback = (QrCodeBoofCVParser.ParseResult) -> Unit
