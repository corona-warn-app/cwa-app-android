package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.qrcode

import android.net.Uri
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import de.rki.coronawarnapp.qrcode.QrCodeFileParser
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.qrcode.parser.QrCodeBoofCVParser
import de.rki.coronawarnapp.qrcode.scanner.ImportDocumentException
import de.rki.coronawarnapp.qrcode.scanner.ImportDocumentException.ErrorCode.CANT_READ_FILE
import de.rki.coronawarnapp.tag
import de.rki.coronawarnapp.util.permission.CameraSettings
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class OrganizerWarnQrCodeScannerViewModel @AssistedInject constructor(
    private val checkInQrCodeExtractor: CheckInQrCodeExtractor,
    private val cameraSettings: CameraSettings,
    private val checkInQrCodeHandler: CheckInQrCodeHandler,
    private val qrCodeFileParser: QrCodeFileParser
) : CWAViewModel() {

    val events = SingleLiveEvent<OrganizerWarnQrCodeNavigation>()

    fun onNavigateUp() {
        events.postValue(OrganizerWarnQrCodeNavigation.BackNavigation)
    }

    fun onImportFile(fileUri: Uri) = launch {
        events.postValue(OrganizerWarnQrCodeNavigation.InProgress)
        Timber.tag(TAG).d("onImportFile(fileUri=$fileUri)")
        try {
            when (val parseResult = qrCodeFileParser.decodeQrCodeFile(fileUri)) {
                is QrCodeFileParser.ParseResult.Failure -> {
                    Timber.tag(TAG).d(parseResult.exception, "parseResult failed")
                    events.postValue(OrganizerWarnQrCodeNavigation.Error(parseResult.exception))
                }
                is QrCodeFileParser.ParseResult.Success -> {
                    Timber.tag(TAG).d("parseResult=$parseResult")
                    onScanResult(parseResult.text)
                }
            }
        } catch (exception: Exception) {
            Timber.tag(TAG).d(exception, "onImportFile($fileUri) failed")
            events.postValue(OrganizerWarnQrCodeNavigation.Error(ImportDocumentException(CANT_READ_FILE)))
        }
    }

    private fun onScanResult(rawResult: String) = launch {
        events.postValue(OrganizerWarnQrCodeNavigation.InProgress)
        try {
            Timber.i("rawResult: $rawResult")
            val checkInQrCode = checkInQrCodeExtractor.extract(rawResult)
            when (val result = checkInQrCodeHandler.handleQrCode(checkInQrCode)) {
                is CheckInQrCodeHandler.Result.Invalid -> events.postValue(
                    OrganizerWarnQrCodeNavigation.InvalidQrCode(result.errorTextRes.toResolvingString())
                )
                is CheckInQrCodeHandler.Result.Valid -> events.postValue(
                    OrganizerWarnQrCodeNavigation.DurationSelectionScreen(result.verifiedTraceLocation.traceLocation)
                )
            }
        } catch (e: Exception) {
            Timber.d(e, "TraceLocation verification failed")
            val msg = e.message ?: "QR-Code was invalid"
            events.postValue(OrganizerWarnQrCodeNavigation.InvalidQrCode(msg.toLazyString()))
        }
    }

    fun setCameraDeniedPermanently(denied: Boolean) {
        Timber.d("setCameraDeniedPermanently(denied=$denied)")
        cameraSettings.isCameraDeniedPermanently.update { denied }
    }

    fun onParseResult(parseResult: QrCodeBoofCVParser.ParseResult) {
        Timber.tag(TAG).d("onParseResult(parseResult=%s)", parseResult)
        parseResult.rawResults.firstOrNull()?.let { onScanResult(rawResult = it) }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<OrganizerWarnQrCodeScannerViewModel>

    companion object {
        private val TAG = tag<OrganizerWarnQrCodeScannerViewModel>()
    }
}
