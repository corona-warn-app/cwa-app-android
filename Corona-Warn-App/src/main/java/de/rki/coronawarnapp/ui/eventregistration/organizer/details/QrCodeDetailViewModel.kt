package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import android.graphics.Bitmap
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.events.TraceLocationUrlGenerator
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import org.joda.time.Instant
import timber.log.Timber

class QrCodeDetailViewModel @AssistedInject constructor(
    @Assisted private val traceLocationId: Long?,
    private val dispatcher: DispatcherProvider,
    private val qrCodeGenerator: QrCodeGenerator,
    private val traceLocationUrlGenerator: TraceLocationUrlGenerator,
    private val traceLocationRepository: DefaultTraceLocationRepository
) : CWAViewModel() {

    private val traceLocationFlow = MutableStateFlow<TraceLocation?>(null)
    private val titleFlow = MutableStateFlow<String?>(null)
    private val subtitleFlow = MutableStateFlow<String?>(null)
    private val startTimeFlow = MutableStateFlow<Instant?>(null)
    private val endTimeFlow = MutableStateFlow<Instant?>(null)

    init {

        launch {
            val traceLocation = traceLocationRepository.traceLocationForId(traceLocationId ?: 0L)

            if (titleFlow.value == null) {
                titleFlow.value = traceLocation.description
            }

            if (subtitleFlow.value == null) {
                subtitleFlow.value = traceLocation.address
            }

            if (startTimeFlow.value == null) {
                startTimeFlow.value = traceLocation.startDate
            }

            if (endTimeFlow.value == null) {
                endTimeFlow.value = traceLocation.endDate
            }

            traceLocationFlow.value = traceLocation

            createQrCode(traceLocation)
        }
    }

    val uiState = combine(
        traceLocationFlow.filterNotNull(),
        startTimeFlow,
        endTimeFlow
    ) { traceLocation, startTime, endTime ->
        UiState(
            traceLocation = traceLocation,
            startInstant = startTime ?: traceLocation.startDate,
            endInstant = endTime ?: traceLocation.endDate
        )
    }.asLiveData()

    data class UiState(
        private val traceLocation: TraceLocation,
        private val startInstant: Instant?,
        private val endInstant: Instant?
    ) {
        val description: String get() = traceLocation.description
        val address: String get() = traceLocation.address
        val startDateTime: Instant? get() = startInstant
        val endDateTime: Instant? get() = endInstant
    }

    val qrCodeBitmap = SingleLiveEvent<Bitmap>()
    val errorMessage = SingleLiveEvent<String>()
    val routeToScreen: SingleLiveEvent<QrCodeDetailNavigationEvents> = SingleLiveEvent()

    /**
     * Creates a QR Code [Bitmap] ,result is delivered by [qrCodeBitmap]
     */
    private fun createQrCode(traceLocation: TraceLocation) = launch(context = dispatcher.IO) {
        try {
            val input = traceLocationUrlGenerator.traceLocationUrl(traceLocation)
            Timber.d("input=$input")
            qrCodeBitmap.postValue(qrCodeGenerator.createQrCode(input))
        } catch (e: Exception) {
            Timber.d(e, "Qr code creation failed")
            errorMessage.postValue(e.localizedMessage ?: "QR code creation failed")
        }
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(QrCodeDetailNavigationEvents.NavigateBack)
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodeDetailViewModel> {
        fun create(
            traceLocationId: Long?
        ): QrCodeDetailViewModel
    }
}
