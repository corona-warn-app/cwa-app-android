package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCode
import de.rki.coronawarnapp.qrcode.QrCodeFileParser
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.qrcode.handler.CoronaTestQrCodeHandler
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.qrcode.scanner.QrCodeValidator
import de.rki.coronawarnapp.tag
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
    private val dccHandler: DccQrCodeHandler,
    private val checkInHandler: CheckInQrCodeHandler,
    private val coronaTestHandler: CoronaTestQrCodeHandler,
) : CWAViewModel(dispatcherProvider) {

    val error = SingleLiveEvent<Throwable>()
    val navEvent = SingleLiveEvent<String>() // TODO Change to event

    fun onImportFile(fileUri: Uri) = launch {
        Timber.tag(TAG).d("onImportFile(fileUri=$fileUri)")
        when (val parseResult = qrCodeFileParser.decodeQrCodeFile(fileUri)) {
            is QrCodeFileParser.ParseResult.Failure -> {
                Timber.tag(TAG).d(parseResult.exception, "parseResult failed")
                // TODO show error message
            }
            is QrCodeFileParser.ParseResult.Success -> {
                Timber.tag(TAG).d("parseResult=$parseResult")
                onScanResult(parseResult.text)
            }
        }
    }

    fun onScanResult(rawResult: String) = launch {
        Timber.tag(TAG).d("onScanResult(rawResult=$rawResult)")
        try {
            when (val qrCode = qrCodeValidator.validate(rawResult)) {
                is CoronaTestQRCode -> onCoronaTestQrCode(qrCode)
                is CheckInQrCode -> onCheckInQrCode(qrCode)
                is DccQrCode -> onDccQrCode(qrCode)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "onScanResult failed")
            error.postValue(e)
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    private suspend fun onDccQrCode(qrCode: DccQrCode) {
        Timber.tag(TAG).d("onDccQrCode=$qrCode")
        val containerId = dccHandler.handleQrCode(qrCode)
        // TODO open certificate details
        Timber.tag(TAG).d("containerId=$containerId")
        navEvent.postValue(containerId.toString())
    }

    private fun onCheckInQrCode(qrCode: CheckInQrCode) {
        Timber.tag(TAG).d("onCheckInQrCode=$qrCode")
        val result = checkInHandler.handleQrCode(qrCode)
        // TODO navigate or show error based on result
        Timber.tag(TAG).d("result=$result")
        navEvent.postValue(result.toString())
    }

    private suspend fun onCoronaTestQrCode(qrCode: CoronaTestQRCode) {
        Timber.tag(TAG).d("onCoronaTestQrCode=$qrCode")
        val submissionEvent = coronaTestHandler.handleQrCode(qrCode)
        // TODO navigate or show error based on result
        Timber.tag(TAG).d("submissionEvent=$submissionEvent")
        navEvent.postValue(submissionEvent.toString())
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeScannerViewModel>

    companion object {
        private val TAG = tag<QrCodeScannerViewModel>()
    }
}
