package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocationQRCode
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class CheckInsViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val qrCodeVerifier: QRCodeVerifier
) : CWAViewModel(dispatcherProvider) {

    val navigationRoutes = SingleLiveEvent<TraceLocationQRCode>()

    fun verifyTraceLocation(uri: String) = launch {
        try {
            Timber.i("uri: $uri")
            val traceLocationQRCode = qrCodeVerifier.verify(uri)
            Timber.i("traceLocationQRCode: $traceLocationQRCode")
            navigationRoutes.postValue(traceLocationQRCode)
        } catch (e: Exception) {
            Timber.d(e, "TraceLocation verification failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CheckInsViewModel>
}
