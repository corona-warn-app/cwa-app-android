package de.rki.coronawarnapp.eventregistration.events.ui.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import de.rki.coronawarnapp.ui.durationpicker.toReadableDuration
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.TraceLocationCreateState
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.category.TraceLocationUIType
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.device.SystemInfoProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.util.Locale
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class TraceLocationCreateViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val typeValue: Int,
    @Assisted private val uiType: TraceLocationUIType
) :
    CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val mutableState = MutableLiveData<TraceLocationCreateState>()
    val state: LiveData<TraceLocationCreateState>
        get() = mutableState

    private val mutableUiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState>
        get() = mutableUiState

    data class UIState(
        private val startDate: LocalDateTime? = null,
        private val endDate: LocalDateTime? = null,
        private val lengthOfStay: Duration? = null,
        val isDateVisible: Boolean,
        val isSendEnable: Boolean
    ) {
        fun getStartDate(locale: Locale): String? {
            return startDate?.toString("E, ${locale.shortDatePattern()}   HH:mm", locale)
        }

        fun getEndDate(locale: Locale): String? {
            return endDate?.toString("E, ${locale.shortDatePattern()}   HH:mm", locale)
        }

        fun getLength(): String? { //TODO: add suffix
            return lengthOfStay?.toReadableDuration()
        }
    }

    class UpdateDelegate<T> : ReadWriteProperty<TraceLocationCreateViewModel?, T?> {
        var value: T? = null

        override fun setValue(
            thisRef: TraceLocationCreateViewModel?,
            property: KProperty<*>,
            value: T?
        ) {
            if (value != null) {
                this.value = value
            }
            thisRef?.updateState()
        }

        override fun getValue(thisRef: TraceLocationCreateViewModel?, property: KProperty<*>): T? {
            return this.value
        }
    }

    private fun updateState() {
        mutableUiState.value = UIState(
            startDate = start,
            endDate = end,
            lengthOfStay = checkInLength,
            isDateVisible = uiType == TraceLocationUIType.EVENT,
            isSendEnable = when (uiType) {
                TraceLocationUIType.LOCATION -> description?.trim()?.length in 1..100 && address?.trim()?.length in 0..100 && (checkInLength
                    ?: Duration.ZERO) > Duration.ZERO
                TraceLocationUIType.EVENT -> description?.trim()?.length in 1..100 && address?.trim()?.length in 0..100 && start != null && end != null && end?.isAfter(start) == true
            }
        )
    }

    var description: String? by UpdateDelegate()
    var address: String? by UpdateDelegate()
    var checkInLength: Duration? by UpdateDelegate()
    var start: LocalDateTime? by UpdateDelegate()
    var end: LocalDateTime? by UpdateDelegate()

    init {
        checkInLength = when (uiType) {
            TraceLocationUIType.LOCATION -> {
                Duration.standardHours(2)
            }
            TraceLocationUIType.EVENT -> {
                Duration.ZERO
            }
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TraceLocationCreateViewModel> {
        fun create(typeValue: Int, uiType: TraceLocationUIType): TraceLocationCreateViewModel
    }
}
