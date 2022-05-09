package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.checkout.CheckOutHandler
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import de.rki.coronawarnapp.qrcode.handler.CheckInQrCodeHandler
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.ActiveCheckInVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.CheckInsItem
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.PastCheckInVH
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.flow.intervalFlow
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.ui.toLazyString
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import timber.log.Timber

@Suppress("LongParameterList")
class CheckInsViewModel @AssistedInject constructor(
    @Assisted private val savedState: SavedStateHandle,
    @Assisted private val deepLink: String?,
    @Assisted private val cleanHistory: Boolean,
    dispatcherProvider: DispatcherProvider,
    @AppScope private val appScope: CoroutineScope,
    private val checkInQrCodeExtractor: CheckInQrCodeExtractor,
    private val checkInsRepository: CheckInRepository,
    private val checkOutHandler: CheckOutHandler,
    private val checkInQrCodeHandler: CheckInQrCodeHandler
) : CWAViewModel(dispatcherProvider) {

    val events = SingleLiveEvent<CheckInEvent>()
    val errorEvent = SingleLiveEvent<Throwable>()

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

    val checkins: LiveData<List<CheckInsItem>> = combine(
        intervalFlow(1000),
        checkInsRepository.checkInsWithinRetention
    ) { _, checkIns ->
        mutableListOf<CheckInsItem>().apply {
            addAll(mapCheckIns(checkIns))
        }
    }.asLiveData(context = dispatcherProvider.Default)

    fun onRemoveCheckInConfirmed(checkIn: CheckIn?) {
        Timber.d("removeCheckin(checkIn=%s)", checkIn)
        launch(scope = appScope) {
            if (checkIn == null) {
                checkInsRepository.reset()
            } else {
                checkInsRepository.deleteCheckIns(listOf(checkIn))
            }
        }
    }

    fun onRemoveAllCheckIns() {
        Timber.d("onRemoveAllCheckIns()")
        events.postValue(CheckInEvent.ConfirmRemoveAll)
    }

    fun onInformationClicked() {
        Timber.d("onInformationClicked()")
        events.postValue(CheckInEvent.ShowInformation)
    }

    private fun mapCheckIns(checkIns: List<CheckIn>): List<CheckInsItem> = run {
        val active = checkIns.filter { !it.completed }.sortedBy { it.checkInEnd }
        val completed = checkIns.filter { it.completed }.sortedByDescending { it.checkInEnd }
        active + completed
    }
        .map { checkin ->
            when {
                !checkin.completed -> ActiveCheckInVH.Item(
                    checkin = checkin,
                    onCardClicked = { checkIn, position ->
                        events.postValue(CheckInEvent.EditCheckIn(checkIn.id, position))
                    },
                    onRemoveItem = { events.postValue(CheckInEvent.ConfirmRemoveItem(it)) },
                    onCheckout = { doCheckOutNow(it) },
                    onSwipeItem = { checkIn, position ->
                        events.postValue(CheckInEvent.ConfirmSwipeItem(checkIn, position))
                    }
                )
                else -> PastCheckInVH.Item(
                    checkin = checkin,
                    onCardClicked = { checkIn, position ->
                        events.postValue(CheckInEvent.EditCheckIn(checkIn.id, position))
                    },
                    onRemoveItem = { events.postValue(CheckInEvent.ConfirmRemoveItem(it)) },
                    onSwipeItem = { checkIn, position ->
                        events.postValue(CheckInEvent.ConfirmSwipeItem(checkIn, position))
                    }
                )
            }
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

    private fun verifyUri(uri: String) = launch {
        try {
            Timber.i("uri: $uri")
            val checkInQrCode = checkInQrCodeExtractor.extract(uri)
            when (val result = checkInQrCodeHandler.handleQrCode(checkInQrCode)) {
                is CheckInQrCodeHandler.Result.Valid -> events.postValue(
                    if (cleanHistory)
                        CheckInEvent.ConfirmCheckInWithoutHistory(result.verifiedTraceLocation)
                    else
                        CheckInEvent.ConfirmCheckIn(result.verifiedTraceLocation)
                )
                is CheckInQrCodeHandler.Result.Invalid -> events.postValue(
                    CheckInEvent.InvalidQrCode(result.errorTextRes.toResolvingString())
                )
            }
        } catch (e: Exception) {
            Timber.d(e, "TraceLocation verification failed")
            val msg = e.message ?: "QR-Code was invalid"
            events.postValue(CheckInEvent.InvalidQrCode(msg.toLazyString()))
        }
    }

    companion object {
        private const val SKEY_LAST_DEEPLINK = "deeplink.last"
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CheckInsViewModel> {
        fun create(
            savedState: SavedStateHandle,
            deepLink: String?,
            cleanHistory: Boolean
        ): CheckInsViewModel
    }
}
