package de.rki.coronawarnapp.ui.eventregistration.attendee.checkins

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QRCodeUriParser
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocationQRCodeVerifier
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocationVerifyResult
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items.ActiveCheckInVH
import de.rki.coronawarnapp.ui.eventregistration.attendee.checkins.items.PastCheckInVH
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.joda.time.Duration
import org.joda.time.Instant
import timber.log.Timber

class CheckInsViewModel @AssistedInject constructor(
    @Assisted private val savedState: SavedStateHandle,
    @Assisted private val deepLink: String?,
    dispatcherProvider: DispatcherProvider,
    private val traceLocationQRCodeVerifier: TraceLocationQRCodeVerifier,
    private val qrCodeUriParser: QRCodeUriParser
) : CWAViewModel(dispatcherProvider) {

    val confirmationEvent = SingleLiveEvent<TraceLocationVerifyResult>()

    val checkins = FAKE_CHECKIN_SOURCE
        .map { checkins -> checkins.sortedBy { it.checkInEnd } }
        .map { checkins ->
            checkins.map { checkin ->
                when {
                    checkin.checkInEnd == null -> ActiveCheckInVH.Item(
                        checkin = checkin,
                        onCardClicked = { /* TODO */ },
                        onRemoveItem = { /* TODO */ },
                        onCheckout = { /* TODO */ }
                    )
                    else -> PastCheckInVH.Item(
                        checkin = checkin,
                        onCardClicked = { /* TODO */ },
                        onRemoveItem = { /* TODO */ }
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

    private fun verifyUri(uri: String) = launch {
        try {
            Timber.i("uri: $uri")
            val signedTraceLocation = qrCodeUriParser.getSignedTraceLocation(uri)
                ?: throw IllegalArgumentException("Invalid uri: $uri")

            val verifyResult = traceLocationQRCodeVerifier.verify(signedTraceLocation.toByteArray())
            Timber.i("verifyResult: $verifyResult")
            confirmationEvent.postValue(verifyResult)
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

private val FAKE_CHECKINS = listOf(
    CheckIn(
        id = 1,
        guid = "testGuid2",
        version = 1,
        type = 1,
        description = "Jahrestreffen der deutschen SAP Anwendergruppe",
        address = "Hauptstr. 3, 69115 Heidelberg",
        traceLocationStart = null,
        traceLocationEnd = null,
        defaultCheckInLengthInMinutes = 3 * 60,
        signature = "Signature",
        checkInStart = Instant.now().minus(Duration.standardHours(2)),
        checkInEnd = null,
        targetCheckInEnd = Instant.now().plus(Duration.standardHours(1)),
        createJournalEntry = true
    ),
    CheckIn(
        id = 2,
        guid = "testGuid1",
        version = 1,
        type = 2,
        description = "CWA Launch Party",
        address = "At home! Do you want the 'rona?",
        traceLocationStart = Instant.parse("2021-01-01T12:00:00.000Z"),
        traceLocationEnd = Instant.parse("2021-01-01T15:00:00.000Z"),
        defaultCheckInLengthInMinutes = 15,
        signature = "Signature",
        checkInStart = Instant.parse("2021-01-01T12:30:00.000Z"),
        checkInEnd = Instant.parse("2021-01-01T14:00:00.000Z"),
        targetCheckInEnd = Instant.parse("2021-01-01T12:45:00.000Z"),
        createJournalEntry = true
    )
)

private val FAKE_CHECKIN_SOURCE = flow {
    emit(FAKE_CHECKINS + FAKE_CHECKINS + FAKE_CHECKINS + FAKE_CHECKINS + FAKE_CHECKINS)
}
