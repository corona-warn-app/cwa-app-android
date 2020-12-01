package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.submission.SubmissionSettings
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate

class SubmissionSymptomCalendarViewModel @AssistedInject constructor(
    @Assisted private val symptomIndication: Symptoms.Indication,
    dispatcherProvider: DispatcherProvider,
    private val submissionSettings: SubmissionSettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val symptomStart = submissionSettings.symptoms.flow
        .map { it.startOfSymptoms }
        .asLiveData(context = dispatcherProvider.Default)

    val routeToScreen: SingleLiveEvent<SubmissionNavigationEvents> = SingleLiveEvent()

    fun onCalendarNextClicked() {
        launch {
            val symptoms = submissionSettings.symptoms.value
            routeToScreen.postValue(
                SubmissionNavigationEvents.NavigateToResultPositiveOtherWarning(symptoms)
            )
        }
    }

    fun onCalendarPreviousClicked() {
        routeToScreen.postValue(SubmissionNavigationEvents.NavigateToSymptomIntroduction)
    }

    fun onLastSevenDaysStart() {
        updateSymptomStart(Symptoms.StartOf.LastSevenDays)
    }

    fun onOneToTwoWeeksAgoStart() {
        updateSymptomStart(Symptoms.StartOf.OneToTwoWeeksAgo)
    }

    fun onMoreThanTwoWeeksStart() {
        updateSymptomStart(Symptoms.StartOf.MoreThanTwoWeeks)
    }

    fun onNoInformationStart() {
        updateSymptomStart(Symptoms.StartOf.NoInformation)
    }

    fun onDateSelected(localDate: LocalDate?) {
        updateSymptomStart(localDate?.let { Symptoms.StartOf.Date(it) })
    }

    private fun updateSymptomStart(startOf: Symptoms.StartOf?) {
        submissionSettings.symptoms.update {
            it.copy(startOfSymptoms = startOf)
        }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<SubmissionSymptomCalendarViewModel> {

        fun create(symptomIndication: Symptoms.Indication): SubmissionSymptomCalendarViewModel
    }
}
