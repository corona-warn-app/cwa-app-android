package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCode
import de.rki.coronawarnapp.qrcode.QrCodeFileParser
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.qrcode.scanner.QrCodeValidator
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

class QrCodeScannerViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val cameraSettings: CameraSettings,
    private val qrCodeValidator: QrCodeValidator,
    private val qrCodeFileParser: QrCodeFileParser,
    private val dccHandler: DccQrCodeHandler,
    private val checkInHandler: CheckInQrCodeHandler,
    private val submissionRepository: SubmissionRepository,
    private val dccSettings: CovidCertificateSettings,
) : CWAViewModel(dispatcherProvider) {

    val result = SingleLiveEvent<ScannerResult>()

    fun onImportFile(fileUri: Uri) = launch {
        result.postValue(InProgress)
        Timber.tag(TAG).d("onImportFile(fileUri=$fileUri)")
        when (val parseResult = qrCodeFileParser.decodeQrCodeFile(fileUri)) {
            is QrCodeFileParser.ParseResult.Failure -> {
                Timber.tag(TAG).d(parseResult.exception, "parseResult failed")
                result.postValue(Error(error = parseResult.exception))
            }
            is QrCodeFileParser.ParseResult.Success -> {
                Timber.tag(TAG).d("parseResult=$parseResult")
                onScanResult(parseResult.text)
            }
        }
    }

    fun onScanResult(rawResult: String) = launch {
        result.postValue(InProgress)
        Timber.tag(TAG).d("onScanResult(rawResult=$rawResult)")
        try {
            when (val qrCode = qrCodeValidator.validate(rawResult)) {
                is CoronaTestQRCode -> onCoronaTestQrCode(qrCode, rawResult)
                is CheckInQrCode -> onCheckInQrCode(qrCode)
                is DccQrCode -> onDccQrCode(qrCode)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "onScanResult failed")
            result.postValue(Error(error = e))
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    private suspend fun onDccQrCode(qrCode: DccQrCode) {
        Timber.tag(TAG).d("onDccQrCode=$qrCode")
        val containerId = dccHandler.handleQrCode(qrCode)
        Timber.tag(TAG).d("containerId=$containerId")
        result.postValue(containerId.toDccResult(!dccSettings.isOnboarded.value))
    }

    private fun onCheckInQrCode(qrCode: CheckInQrCode) {
        Timber.tag(TAG).d("onCheckInQrCode=$qrCode")
        val checkInResult = checkInHandler.handleQrCode(qrCode)
        Timber.tag(TAG).d("checkInResult=$checkInResult")
        result.postValue(checkInResult.toCheckInResult())
    }

    private suspend fun onCoronaTestQrCode(qrCode: CoronaTestQRCode, rawResult: String) {
        Timber.tag(TAG).d("onCoronaTestQrCode=$qrCode")
        val coronaTest = submissionRepository.testForType(qrCode.type).first()
        val coronaTestResult = if (coronaTest != null) {
            CoronaTestResult.DuplicateTest(rawResult)
        } else {
            CoronaTestResult.ConsentTest(rawResult)
        }
        Timber.tag(TAG).d("coronaTestResult=$coronaTestResult")
        result.postValue(coronaTestResult)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeScannerViewModel>

    companion object {
        private val TAG = tag<QrCodeScannerViewModel>()
    }
}
