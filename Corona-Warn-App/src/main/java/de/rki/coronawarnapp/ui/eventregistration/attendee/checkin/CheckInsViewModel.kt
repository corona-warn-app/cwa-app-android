package de.rki.coronawarnapp.ui.eventregistration.attendee.checkin

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.EventQRCode
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.isValidQRCodeUri
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

    val navigationRoutes = SingleLiveEvent<EventQRCode>()

    fun verifyEvent(encodedEvent: String) = launch {
        try {
            encodedEvent.isValidQRCodeUri()
            Timber.i("encodedEvent: $encodedEvent")
            val eventQRCode = qrCodeVerifier.verify(encodedEvent)
            Timber.i("eventQRCode: $eventQRCode")
            navigationRoutes.postValue(eventQRCode)
        } catch (e: Exception) {
            Timber.d(e, "Event verification failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<CheckInsViewModel>
}
