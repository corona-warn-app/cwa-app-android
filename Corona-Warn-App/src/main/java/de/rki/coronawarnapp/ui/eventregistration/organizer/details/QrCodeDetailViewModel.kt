package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import android.graphics.Bitmap
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.ui.eventregistration.attendee.edit.EditCheckInViewModel
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

class QrCodeDetailViewModel @AssistedInject constructor(
    //@Assisted private val eventId: String?,
    private val dispatcher: DispatcherProvider,
    private val qrCodeGenerator: QrCodeGenerator,
    private val traceLocationRepository: DefaultTraceLocationRepository
) : CWAViewModel() {

    /* Currently hardcoded to show the functionality -> will be connected to the database in a separate PR */
    /* val qrCodeText = "HTTPS://E.CORONAWARN.APP/C1/BIYAUEDBZY6EIWF7QX6JOKSRPAGEB3H7CIIEGV2BEBGGC5LOMNUCAUD" +
        "BOJ2HSGGTQ6SACIHXQ6SACKA6CJEDARQCEEAPHGEZ5JI2K2T422L5U3SMZY5DGCPUZ2RQACAYEJ3HQYMAFF" +
        "BU2SQCEEAJAUCJSQJ7WDM675MCMOD3L2UL7ECJU7TYERH23B746RQTABO3CTI="
    val titleText = "Jahrestreffen der deutschen SAP Anwendergruppe"
    val subtitleText = "Hauptstr 3, 69115 Heidelberg"
    val eventDate = "21.01.2021, 18:00 - 21:00 Uhr" */

    private val traceLocationFlow = MutableStateFlow<TraceLocation?>(null)
    private val titleFlow = MutableStateFlow<String?>(null)
    private val subtitleFlow = MutableStateFlow<String?>(null)
    private val startTimeFlow = MutableStateFlow<Instant?>(null)
    private val endTimeFlow = MutableStateFlow<Instant?>(null)

    private val eventId = ""
    init {

        launch {
            val traceLocation = traceLocationRepository.traceLocationForId(eventId?: "")

            if(titleFlow.value == null) {
                titleFlow.value = traceLocation.description;
            }

            if(subtitleFlow.value == null) {
                subtitleFlow.value = traceLocation.address;
            }

            if(startTimeFlow.value == null) {
                startTimeFlow.value = traceLocation.startDate;
            }

            if(endTimeFlow.value == null) {
                endTimeFlow.value = traceLocation.endDate;
            }

            traceLocationFlow.value = traceLocation

            createQrCode("Hello World!")

        }

    }

    val uiState = combine(
        traceLocationFlow.filterNotNull(),
        startTimeFlow,
        endTimeFlow
    ) { traceLocation, startTime, endTime ->
        UiState(
            traceLocation = traceLocation,
            startInstant = startTime ?: traceLocation.startDate!!,
            endInstant = endTime ?: traceLocation.endDate!!
        )
    }.asLiveData()


    data class UiState(
        private val traceLocation: TraceLocation,
        private val startInstant: Instant,
        private val endInstant: Instant
    ) {
        val description: String get() = traceLocation.description
        val address: String get() = traceLocation.address
        val startTime: String get() = startInstant.toDateTime().toString(timeFormatter)
        val startDate: String get() = startInstant.toDateTime().toString(dateFormatter)
        val endTime: String get() = endInstant.toDateTime().toString(timeFormatter)
        val endDate: String get() = endInstant.toDateTime().toString(dateFormatter)
    }


    val qrCodeBitmap = SingleLiveEvent<Bitmap>()
    val errorMessage = SingleLiveEvent<String>()

    val routeToScreen: SingleLiveEvent<QrCodeDetailNavigationEvents> = SingleLiveEvent()

    /**
     * Creates a QR Code [Bitmap] ,result is delivered by [qrCodeBitmap]
     */
    fun createQrCode(input: String) = launch(context = dispatcher.IO) {

        try {
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
    interface Factory : SimpleCWAViewModelFactory<QrCodeDetailViewModel>

    /*
    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodeDetailViewModel> {
        fun create(
            eventId: String?
        ): QrCodeDetailViewModel
    } */
}

private val dateFormatter by lazy {
    DateTimeFormat.forPattern("dd.MM.yyyy")
}

private val timeFormatter by lazy {
    DateTimeFormat.forPattern("HH:mm")
}
