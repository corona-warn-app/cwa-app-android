package de.rki.coronawarnapp.qrcode.ui

import android.net.Uri
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccMaxPersonChecker
import de.rki.coronawarnapp.covidcertificate.common.qrcode.DccQrCode
import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.vaccination.core.CovidCertificateSettings
import de.rki.coronawarnapp.dccticketing.core.common.DccTicketingException
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCode
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeHandler
import de.rki.coronawarnapp.presencetracing.TraceLocationSettings
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCode
import de.rki.coronawarnapp.qrcode.QrCodeFileParser
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.qrcode.handler.DccQrCodeHandler
import de.rki.coronawarnapp.qrcode.parser.QrCodeBoofCVParser
import de.rki.coronawarnapp.qrcode.scanner.ImportDocumentException
import de.rki.coronawarnapp.qrcode.scanner.ImportDocumentException.ErrorCode.CANT_READ_FILE
import de.rki.coronawarnapp.qrcode.scanner.QrCodeValidator
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.reyclebin.covidcertificate.RecycledCertificatesProvider
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.first
import timber.log.Timber

@Suppress("LongParameterList")
class QrCodeScannerViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val qrCodeValidator: QrCodeValidator,
    private val qrCodeFileParser: QrCodeFileParser,
    private val dccHandler: DccQrCodeHandler,
    private val checkInHandler: CheckInQrCodeHandler,
    private val dccTicketingQrCodeHandler: DccTicketingQrCodeHandler,
    private val submissionRepository: SubmissionRepository,
    private val dccSettings: CovidCertificateSettings,
    private val traceLocationSettings: TraceLocationSettings,
    private val recycledCertificatesProvider: RecycledCertificatesProvider,
    private val recycledCoronaTestsProvider: RecycledCoronaTestsProvider,
    private val dccMaxPersonChecker: DccMaxPersonChecker
) : CWAViewModel(dispatcherProvider) {

    val result = SingleLiveEvent<ScannerResult>()

    fun onImportFile(fileUri: Uri) = launch {
        result.postValue(InProgress)
        Timber.tag(TAG).d("onImportFile(fileUri=$fileUri)")
        try {
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
        } catch (exception: Exception) {
            Timber.tag(TAG).d(exception, "onImportFile($fileUri) failed")
            result.postValue(Error(ImportDocumentException(CANT_READ_FILE)))
        }
    }

    fun onParseResult(parseResult: QrCodeBoofCVParser.ParseResult) {
        Timber.tag(TAG).d("onParseResult(parseResult=%s)", parseResult)
        when (parseResult) {
            is QrCodeBoofCVParser.ParseResult.Failure -> result.postValue(Error(error = parseResult.exception))
            is QrCodeBoofCVParser.ParseResult.Success -> parseResult.rawResults.firstOrNull()
                ?.let { onScanResult(rawResult = it) }
        }
    }

    private fun onScanResult(rawResult: String) = launch {
        result.postValue(InProgress)
        Timber.tag(TAG).d("onScanResult(rawResult=$rawResult)")
        try {
            when (val qrCode = qrCodeValidator.validate(rawResult)) {
                is CoronaTestQRCode -> onCoronaTestQrCode(qrCode)
                is CheckInQrCode -> onCheckInQrCode(qrCode)
                is DccQrCode -> onDccQrCode(qrCode)
                is DccTicketingQrCode -> onTicketValidationQrCode(qrCode)
            }
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "onScanResult failed")
            result.postValue(Error(error = e))
        }
    }

    private suspend fun onTicketValidationQrCode(qrCode: DccTicketingQrCode) {
        try {
            val transactionContext = dccTicketingQrCodeHandler.handleQrCode(qrCode)
            result.postValue(DccTicketingResult.ConsentI(transactionContext))
        } catch (e: Exception) {
            Timber.tag(TAG).d(e, "onTicketValidationQrCode failed")
            val error = when (e) {
                is DccTicketingException -> DccTicketingError(
                    error = e,
                    errorMsg = e.errorMessage(serviceProvider = qrCode.data.serviceProvider)
                )
                else -> Error(error = e)
            }
            result.postValue(error)
        }
    }

    fun onInfoButtonPress() {
        result.postValue(InfoScreen)
    }

    fun restoreCertificate(containerId: CertificateContainerId) = launch {
        Timber.tag(TAG).d("restoreCertificate(containerId=%s)", containerId)
        recycledCertificatesProvider.restoreCertificate(containerId)
        result.postValue(containerId.toDccDetails())
    }

    fun restoreCoronaTest(recycledCoronaTest: CoronaTest) = launch {
        val currentCoronaTest = submissionRepository.testForType(recycledCoronaTest.type).first()
        when {
            currentCoronaTest != null -> CoronaTestResult.RestoreDuplicateTest(
                recycledCoronaTest.toRestoreRecycledTestRequest()
            )

            else -> {
                recycledCoronaTestsProvider.restoreCoronaTest(recycledCoronaTest.identifier)
                recycledCoronaTest.toCoronaTestResult()
            }
        }.also {
            result.postValue(it)
        }
    }

    private fun CoronaTest.toCoronaTestResult(): CoronaTestResult = when {
        isPending -> CoronaTestResult.TestPending(test = this)
        isNegative -> CoronaTestResult.TestNegative(test = this)
        isPositive -> when (isAdvancedConsentGiven) {
            true -> CoronaTestResult.TestPositive(test = this)
            false -> CoronaTestResult.WarnOthers(test = this)
        }
        else -> CoronaTestResult.TestInvalid(test = this)
    }

    private suspend fun onDccQrCode(dccQrCode: DccQrCode) {
        Timber.tag(TAG).d("onDccQrCode()")

        val recycledContainerId = recycledCertificatesProvider.findCertificate(dccQrCode.qrCode)
        val event = when {
            recycledContainerId != null -> {
                Timber.tag(TAG).d("recycledContainerId=$recycledContainerId")
                DccResult.InRecycleBin(recycledContainerId)
            }
            dccSettings.isOnboarded.value -> {
                when (val checkerResult = dccMaxPersonChecker.checkForMaxPersons(dccQrCode)) {
                    DccMaxPersonChecker.Result.Passed -> {
                        val containerId = dccHandler.handleQrCode(dccQrCode = dccQrCode)
                        Timber.tag(TAG).d("containerId=%s,checkerResult=%s", containerId, checkerResult)
                        containerId.toDccDetails()
                    }
                    is DccMaxPersonChecker.Result.ReachesThreshold -> {
                        val containerId = dccHandler.handleQrCode(dccQrCode = dccQrCode)
                        Timber.tag(TAG).d("containerId=%s,checkerResult=%s", containerId, checkerResult)
                        containerId.toMaxPersonsWarning(checkerResult.max)
                    }
                    is DccMaxPersonChecker.Result.ExceedsMax -> {
                        Timber.tag(TAG).w("Importing new certificate is blocked")
                        DccResult.MaxPersonsBlock(checkerResult.max)
                    }
                }
            }
            else -> DccResult.Onboarding(dccQrCode)
        }
        result.postValue(event)
    }

    private fun onCheckInQrCode(qrCode: CheckInQrCode) {
        Timber.tag(TAG).d("onCheckInQrCode()")
        val checkInResult = checkInHandler.handleQrCode(qrCode)
        Timber.tag(TAG).d("checkInResult=${checkInResult::class.simpleName}")
        result.postValue(checkInResult.toCheckInResult(!traceLocationSettings.isOnboardingDone))
    }

    private suspend fun onCoronaTestQrCode(qrCode: CoronaTestQRCode) {
        Timber.tag(TAG).d("onCoronaTestQrCode()")
        val recycledCoronaTest = recycledCoronaTestsProvider.findCoronaTest(qrCode.rawQrCode.toSHA256())

        val coronaTestResult = when {
            recycledCoronaTest != null -> CoronaTestResult.InRecycleBin(recycledCoronaTest)
            else -> CoronaTestResult.TestRegistrationSelection(qrCode)
        }
        Timber.tag(TAG).d("coronaTestResult=${coronaTestResult::class.simpleName}")
        result.postValue(coronaTestResult)
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<QrCodeScannerViewModel>

    companion object {
        private val TAG = tag<QrCodeScannerViewModel>()
    }
}
