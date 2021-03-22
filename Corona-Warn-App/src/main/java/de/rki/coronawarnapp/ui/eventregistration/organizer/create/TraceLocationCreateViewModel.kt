package de.rki.coronawarnapp.ui.eventregistration.organizer.create

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import de.rki.coronawarnapp.ui.durationpicker.toReadableDuration
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.eventregistration.organizer.category.adapter.category.TraceLocationUIType
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import java.util.Locale
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class TraceLocationCreateViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val category: TraceLocationCategory
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val mutableUiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState>
        get() = mutableUiState

    var description: String? by UpdateDelegate()
    var address: String? by UpdateDelegate()
    var checkInLength: Duration? by UpdateDelegate()
    var start: LocalDateTime? by UpdateDelegate()
    var end: LocalDateTime? by UpdateDelegate()

    init {
        checkInLength = when (category.uiType) {
            TraceLocationUIType.LOCATION -> {
                Duration.standardHours(2)
            }
            TraceLocationUIType.EVENT -> {
                Duration.ZERO
            }
        }
    }

    fun send() {
        // TODO: This will be implemented in another PR
    }

    private fun updateState() {
        mutableUiState.value = UIState(
            startDate = start,
            endDate = end,
            lengthOfStay = checkInLength,
            title = category.title,
            isDateVisible = category.uiType == TraceLocationUIType.EVENT,
            isSendEnable = when (category.uiType) {
                TraceLocationUIType.LOCATION -> {
                    description?.trim()?.length in 1..100 &&
                        address?.trim()?.length in 0..100 &&
                        (checkInLength ?: Duration.ZERO) > Duration.ZERO
                }
                TraceLocationUIType.EVENT -> {
                    description?.trim()?.length in 1..100 &&
                        address?.trim()?.length in 0..100 &&
                        start != null &&
                        end != null &&
                        end?.isAfter(start) == true
                }
            }
        )
    }

    data class UIState(
        private val startDate: LocalDateTime? = null,
        private val endDate: LocalDateTime? = null,
        private val lengthOfStay: Duration? = null,
        @StringRes val title: Int,
        val isDateVisible: Boolean,
        val isSendEnable: Boolean
    ) {
        fun getStartDate(locale: Locale): String? {
            return startDate?.toString("E, ${locale.shortDatePattern()}   HH:mm", locale)
        }

        fun getEndDate(locale: Locale): String? {
            return endDate?.toString("E, ${locale.shortDatePattern()}   HH:mm", locale)
        }

        fun getLength(resources: Resources): String? {
            return lengthOfStay?.toReadableDuration(
                suffix = resources.getString(R.string.tracelocation_organizer_duration_suffix)
            )
        }
    }

    private class UpdateDelegate<T> : ReadWriteProperty<TraceLocationCreateViewModel?, T?> {
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

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TraceLocationCreateViewModel> {
        fun create(category: TraceLocationCategory): TraceLocationCreateViewModel
    }
}
