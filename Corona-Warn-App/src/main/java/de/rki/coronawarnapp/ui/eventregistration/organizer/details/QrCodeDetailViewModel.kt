package de.rki.coronawarnapp.ui.eventregistration.organizer.details

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.QrCodeGenerator
import de.rki.coronawarnapp.eventregistration.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.eventregistration.storage.repo.DefaultTraceLocationRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.category.traceLocationCategories
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.Instant
import timber.log.Timber

class QrCodeDetailViewModel @AssistedInject constructor(
    @Assisted private val traceLocationId: Long,
    private val dispatcher: DispatcherProvider,
    private val qrCodeGenerator: QrCodeGenerator,
    private val traceLocationRepository: DefaultTraceLocationRepository
) : CWAViewModel() {

    private var traceLocation: TraceLocation? = null
    private val mutableUiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState>
        get() = mutableUiState
    val routeToScreen: SingleLiveEvent<QrCodeDetailNavigationEvents> = SingleLiveEvent()

    init {
        launch {
            traceLocation = traceLocationRepository.traceLocationForId(traceLocationId).also {
                mutableUiState.postValue(UiState(it))
                createQrCode(it)
            }
        }
    }

    /**
     * Creates a QR Code [Bitmap] ,result is delivered by [qrCodeBitmap]
     */
    private fun createQrCode(traceLocation: TraceLocation) = launch(context = dispatcher.IO) {
        try {
            val input = traceLocation.locationUrl
            Timber.d("input=$input")
            mutableUiState.postValue(UiState(traceLocation, qrCodeGenerator.createQrCode(input)))
        } catch (e: Exception) {
            Timber.d(e, "Qr code creation failed")
            e.report(ExceptionCategory.INTERNAL)
        }
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(QrCodeDetailNavigationEvents.NavigateBack)
    }

    fun onPrintQrCode() {
        routeToScreen.postValue(
            QrCodeDetailNavigationEvents.NavigateToQrCodePosterFragment(traceLocationId)
        )
    }

    fun duplicateTraceLocation() {
        traceLocation?.let {
            val category = traceLocationCategories.find { category -> category.type == it.type }
            if (category == null) {
                Timber.e("Category not found, traceLocation = $traceLocation")
            } else {
                routeToScreen.postValue(QrCodeDetailNavigationEvents.NavigateToDuplicateFragment(it, category))
            }
        }
    }

    data class UiState(
        private val traceLocation: TraceLocation,
        val bitmap: Bitmap? = null
    ) {
        val description: String get() = traceLocation.description
        val address: String get() = traceLocation.address
        val startDateTime: Instant? get() = traceLocation.startDate
        val endDateTime: Instant? get() = traceLocation.endDate
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<QrCodeDetailViewModel> {
        fun create(
            traceLocationId: Long
        ): QrCodeDetailViewModel
    }
}
