package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeUriParser
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocationQRCodeVerifier
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutHandler
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items.ActiveCheckInVH
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items.CheckInsItem
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items.PastCheckInVH
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber

class CheckInsViewModel @AssistedInject constructor(
    @Assisted private val savedState: SavedStateHandle,
    @Assisted private val deepLink: String?,
    dispatcherProvider: DispatcherProvider,
    @AppScope private val appScope: CoroutineScope,
    private val traceLocationQRCodeVerifier: TraceLocationQRCodeVerifier,
    private val qrCodeUriParser: QRCodeUriParser,
    private val checkInsRepository: CheckInRepository,
    private val checkOutHandler: CheckOutHandler,
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<CheckInEvent>()
    val errorEvent = SingleLiveEvent<Throwable>()

    val checkins: LiveData<List<CheckInsItem>> = combine(
        intervalFlow(1000),
        checkInsRepository.allCheckIns
    ) { _, checkins -> checkins }
        .map { checkins ->
            val active = checkins.filter { !it.completed }.sortedBy { it.checkInEnd }
            val completed = checkins.filter { it.completed }.sortedByDescending { it.checkInEnd }
            active + completed
        }
        .map { checkins ->
            checkins.map { checkin ->
                when {
                    !checkin.completed -> ActiveCheckInVH.Item(
                        checkin = checkin,
                        onCardClicked = { events.postValue(CheckInEvent.EditCheckIn(it.id)) },
                        onRemoveItem = { events.postValue(CheckInEvent.ConfirmRemoveItem(it)) },
                        onCheckout = { doCheckOutNow(it) },
                        onSwipeItem = { checkIn, position ->
                            events.postValue(
                                CheckInEvent.ConfirmSwipeItem(
                                    checkIn,
                                    position
                                )
                            )
                        }
                    )
                    else -> PastCheckInVH.Item(
                        checkin = checkin,
                        onCardClicked = { events.postValue(CheckInEvent.EditCheckIn(it.id)) },
                        onRemoveItem = { events.postValue(CheckInEvent.ConfirmRemoveItem(it)) },
                        onSwipeItem = { checkIn, position ->
                            events.postValue(
                                CheckInEvent.ConfirmSwipeItem(
                                    checkIn,
                                    position
                                )
                            )
                        }
                    )
                }
            }
        }
        .asLiveData(context = dispatcherProvider.Default)

    init {
        deepLink?.let {
            if (deepLink != savedState.get(SKEY_LAST_DEEPLINK)) {
                Timber.i("New deeplink: %s", deepLink)
                verifyUri(it)
            } else {
                Timber.d("Already consumed this deeplink: %s", deepLink)
            }
        }
        savedState.set(SKEY_LAST_DEEPLINK, deepLink)
    }

    private fun doCheckOutNow(checkIn: CheckIn) = launch(scope = appScope) {
        Timber.d("doCheckOutNow(checkIn=%s)", checkIn)
        try {
            checkOutHandler.checkOut(checkIn.id)
        } catch (e: Exception) {
            Timber.e(e, "Checkout failed for %s", checkIn)
            errorEvent.postValue(e)
        }
    }

    fun onRemoveCheckInConfirmed(checkIn: CheckIn?) {
        Timber.d("removeCheckin(checkIn=%s)", checkIn)
        launch(scope = appScope) {
            if (checkIn == null) {
                checkInsRepository.clear()
            } else {
                checkInsRepository.deleteCheckIns(listOf(checkIn))
            }
        }
    }

    fun onRemoveAllCheckIns() {
        Timber.d("onRemovaAllCheckIns()")
        events.postValue(CheckInEvent.ConfirmRemoveAll)
    }

    private fun verifyUri(uri: String) = launch {
        try {
            Timber.i("uri: $uri")
            val signedTraceLocation = qrCodeUriParser.getSignedTraceLocation(uri)
                ?: throw IllegalArgumentException("Invalid uri: $uri")

            val verifyResult = traceLocationQRCodeVerifier.verify(signedTraceLocation.toByteArray())
            Timber.i("verifyResult: $verifyResult")
            events.postValue(CheckInEvent.ConfirmCheckIn(verifyResult))
        } catch (e: Exception) {
            Timber.d(e, "TraceLocation verification failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    companion object {
        private const val SKEY_LAST_DEEPLINK = "deeplink.last"
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CheckInsViewModel> {
        fun create(
            savedState: SavedStateHandle,
            deepLink: String?
        ): CheckInsViewModel
    }
}
