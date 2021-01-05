package de.rki.coronawarnapp.ui.submission.symptoms.calendar

import androidx.lifecycle.asLiveData
import androidx.navigation.NavDirections
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.submission.auto.AutoSubmission
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import org.joda.time.LocalDate
import timber.log.Timber

class SubmissionSymptomCalendarViewModel @AssistedInject constructor(
    @Assisted val symptomIndication: Symptoms.Indication,
    dispatcherProvider: DispatcherProvider,
    private val submissionRepository: SubmissionRepository,
    private val autoSubmission: AutoSubmission
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val symptomStartInternal = MutableStateFlow<Symptoms.StartOf?>(null)
    val symptomStart = symptomStartInternal.asLiveData(context = dispatcherProvider.Default)

    val routeToScreen = SingleLiveEvent<NavDirections>()
    val showCancelDialog = SingleLiveEvent<Unit>()
    val showUploadDialog = autoSubmission.isSubmissionRunning
        .asLiveData(context = dispatcherProvider.Default)

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
        symptomStartInternal.value = startOf
    }

    fun onCalendarPreviousClicked() {
        showCancelDialog.postValue(Unit)
    }

    fun onDone() {
        if (symptomStartInternal.value == null) {
            IllegalStateException("Can't finish symptom indication without symptomStart value.")
                .reportProblem(tag = TAG, "UI should not allow symptom submission without start date.")
            return
        }
        Timber.tag(TAG).d("onDone() clicked on calender screen.")
        submissionRepository.currentSymptoms.update {
            Symptoms(
                symptomIndication = symptomIndication,
                startOfSymptoms = symptomStartInternal.value
            ).also { Timber.tag(TAG).v("Symptoms updated to %s", it) }
        }
        performSubmission()
    }

    fun onCancelConfirmed() {
        Timber.d("onCancelConfirmed() clicked on calendar screen.")
        performSubmission()
    }

    private fun performSubmission() {
        launch {
            try {
                autoSubmission.runSubmissionNow()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "performSubmission() failed.")
            } finally {
                routeToScreen.postValue(
                    SubmissionSymptomCalendarFragmentDirections.actionSubmissionSymptomCalendarFragmentToMainFragment()
                )
            }
        }
    }

    fun onNewUserActivity() {
        Timber.d("onNewUserActivity()")
        autoSubmission.updateLastSubmissionUserActivity()
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<SubmissionSymptomCalendarViewModel> {

        fun create(symptomIndication: Symptoms.Indication): SubmissionSymptomCalendarViewModel
    }

    companion object {
        private const val TAG = "SymptomsCalenderVM"
    }
}
