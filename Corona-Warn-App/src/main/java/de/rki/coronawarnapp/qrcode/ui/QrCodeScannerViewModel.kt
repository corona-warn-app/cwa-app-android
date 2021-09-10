package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCode
import de.rki.coronawarnapp.qrcode.QrCodeFileParser
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.qrcode.scanner.QrCodeValidator
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.ui.submission.qrcode.scan.SubmissionQRCodeScanViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class QrCodeScannerViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val cameraSettings: CameraSettings,
    private val qrCodeValidator: QrCodeValidator,
    private val qrCodeFileParser: QrCodeFileParser,
    private val dccQrCodeHandler: DccQrCodeHandler,
) : CWAViewModel(dispatcherProvider) {

    val error = SingleLiveEvent<Throwable>()
    val navEvent = SingleLiveEvent<Throwable>()

    fun onImportFile(fileUri: Uri) = launch {
        when (val parseResult = qrCodeFileParser.decodeQrCodeFile(fileUri)) {
            is QrCodeFileParser.QrCodeParseResult.Failure -> {
                Timber.tag(TAG).d(parseResult.exception, "parseResult failed")
                // TODO show error message
            }
            is QrCodeFileParser.QrCodeParseResult.Success -> {
                Timber.tag(TAG).d("parseResult=$parseResult")
                onScanResult(parseResult.text)
            }
        }
    }

    fun onScanResult(rawResult: String) = launch {
        Timber.tag(TAG).d("onScanResult(rawResult=$rawResult)")
        when (val qrCode = qrCodeValidator.validate(rawResult)) {
            is CoronaTestQRCode -> {
                // TODO
            }

            is CheckInQrCode -> {
                // TODO
            }
            is DccQrCode -> dccQrCodeHandler.handleDccQrCode(qrCode)
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeScannerViewModel>

    companion object {
        private val TAG = tag<QrCodeScannerViewModel>()
    }
}
