package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.joda.time.LocalDate

class SubmissionSymptomCalendarViewModel @AssistedInject constructor(
    @Assisted private val symptomIndication: Symptoms.Indication,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val symptomStartInternal = MutableStateFlow<Symptoms.StartOf?>(null)
    val symptomStart = symptomStartInternal
        .asLiveData(context = dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onCalendarNextClicked() {
        launch {
            val symptoms = Symptoms(
                startOfSymptoms = symptomStartInternal.first(),
                symptomIndication = symptomIndication
            )
            routeToScreen.postValue(
                SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning(symptoms)
            )
        }
    }

    fun onCalendarPreviousClicked() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onLastSevenDaysStart() {
        symptomStartInternal.value = Symptoms.StartOf.LastSevenDays
    }

    fun onOneToTwoWeeksAgoStart() {
        symptomStartInternal.value = Symptoms.StartOf.OneToTwoWeeksAgo
    }

    fun onMoreThanTwoWeeksStart() {
        symptomStartInternal.value = Symptoms.StartOf.MoreThanTwoWeeks
    }

    fun onNoInformationStart() {
        symptomStartInternal.value = Symptoms.StartOf.NoInformation
    }

    fun onDateSelected(localDate: LocalDate?) {
        symptomStartInternal.value = localDate?.let { Symptoms.StartOf.Date(it) }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<SubmissionSymptomCalendarViewModel> {

        fun create(symptomIndication: Symptoms.Indication): SubmissionSymptomCalendarViewModel
    }
}
