package de.rki.coronawarnapp.ui.presencetracing.organizer.create

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.censors.presencetracing.TraceLocationCensor
import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocation
import de.rki.coronawarnapp.presencetracing.locations.TraceLocationCreator
import de.rki.coronawarnapp.presencetracing.locations.TraceLocationUserInput
import de.rki.coronawarnapp.ui.durationpicker.toReadableDuration
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationUIType
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.DateTime
import org.joda.time.Duration
import timber.log.Timber
import java.util.Locale
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class TraceLocationCreateViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted private val category: TraceLocationCategory,
    private val traceLocationCreator: TraceLocationCreator
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val result = SingleLiveEvent<Result>()

    private val mutableUiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState>
        get() = mutableUiState

    private var requestInProgress: Boolean by UpdateDelegateWithDefaultValue(false)
    var description: String by UpdateDelegateWithDefaultValue("")
    var address: String by UpdateDelegateWithDefaultValue("")
    var checkInLength: Duration by UpdateDelegateWithDefaultValue(Duration.ZERO)
    var begin: DateTime? by UpdateDelegate()
    var end: DateTime? by UpdateDelegate()

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
        requestInProgress = true

        val userInput = TraceLocationUserInput(
            type = category.type,
            description = description,
            address = address,
            startDate = begin?.toDateTime()?.toInstant(),
            endDate = end?.toDateTime()?.toInstant(),
            defaultCheckInLengthInMinutes = checkInLength.standardMinutes.toInt()
        )

        TraceLocationCensor.dataToCensor = userInput

        launch {
            try {
                val traceLocation = traceLocationCreator.createTraceLocation(userInput)
                result.postValue(Result.Success(traceLocation))
            } catch (exception: Exception) {
                Timber.e(exception, "Something went wrong when sending the event $userInput to the server")
                result.postValue(Result.Error(exception))
            } finally {
                requestInProgress = false
            }
        }
    }

    private fun updateState() {
        mutableUiState.postValue(
            UIState(
                begin = begin,
                end = end,
                checkInLength = checkInLength,
                title = category.title,
                isRequestInProgress = requestInProgress,
                isDateVisible = category.uiType == TraceLocationUIType.EVENT,
                isSendEnable = when (category.uiType) {
                    TraceLocationUIType.LOCATION -> {
                        description.isTextFormattedCorrectly() &&
                            address.isTextFormattedCorrectly() &&
                            checkInLength > Duration.ZERO &&
                            !requestInProgress
                    }
                    TraceLocationUIType.EVENT -> {
                        description.isTextFormattedCorrectly() &&
                            address.isTextFormattedCorrectly() &&
                            begin != null && end != null && end?.isAfter(begin) == true &&
                            !requestInProgress
                    }
                }
            )
        )
    }

    private fun String.isTextFormattedCorrectly() = trim().length in 1..100 && !contains('\n')

    data class UIState(
        private val begin: DateTime? = null,
        private val end: DateTime? = null,
        private val checkInLength: Duration? = null,
        @StringRes val title: Int,
        val isRequestInProgress: Boolean,
        val isDateVisible: Boolean,
        val isSendEnable: Boolean
    ) {
        fun getBegin(locale: Locale) = getFormattedTime(begin, locale)

        fun getEnd(locale: Locale) = getFormattedTime(end, locale)

        fun getCheckInLength(resources: Resources): String? {
            return checkInLength?.toReadableDuration(
                suffix = resources.getString(R.string.tracelocation_organizer_duration_suffix)
            )
        }

        private fun getFormattedTime(value: DateTime?, locale: Locale) =
            value?.toString("E, ${locale.shortDatePattern()}   HH:mm", locale)
    }

    sealed class Result {
        data class Error(val exception: java.lang.Exception) : Result()
        data class Success(val eventEntity: TraceLocation) : Result()
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

    private class UpdateDelegateWithDefaultValue<T>(defaultValue: T) :
        ReadWriteProperty<TraceLocationCreateViewModel?, T> {
        var value: T = defaultValue

        override fun setValue(
            thisRef: TraceLocationCreateViewModel?,
            property: KProperty<*>,
            value: T
        ) {
            this.value = value
            thisRef?.updateState()
        }

        override fun getValue(thisRef: TraceLocationCreateViewModel?, property: KProperty<*>): T {
            return this.value
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<TraceLocationCreateViewModel> {
        fun create(category: TraceLocationCategory): TraceLocationCreateViewModel
    }
}
