package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifyResult
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.isValidQRCodeUri
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class CheckInsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val qrCodeVerifier: QRCodeVerifier
) : CWAViewModel(dispatcherProvider) {

    private val verifyResultData = MutableLiveData<QRCodeVerifyResult>()
    val verifyResult: LiveData<QRCodeVerifyResult> = verifyResultData

    fun verifyUri(uri: String) = launch {
        try {
            Timber.i("uri: $uri")
            if (!uri.isValidQRCodeUri())
                throw IllegalArgumentException("Invalid uri: $uri")

            val encodedEvent = uri.substringAfterLast("/")
            val verifyResult = qrCodeVerifier.verify(encodedEvent)
            Timber.i("verifyResult: $verifyResult")
            verifyResultData.postValue(verifyResult)
        } catch (e: Exception) {
            Timber.d(e, "TraceLocation verification failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CheckInsViewModel>
}
