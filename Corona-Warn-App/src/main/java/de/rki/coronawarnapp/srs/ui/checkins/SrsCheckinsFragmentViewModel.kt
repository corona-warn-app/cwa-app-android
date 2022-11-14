package de.rki.coronawarnapp.srs.ui.checkins

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.common.completedCheckIns
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.CheckInsConsentItem
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.HeaderCheckInsVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent.SelectableCheckInVH
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class SrsCheckinsFragmentViewModel @AssistedInject constructor(
    @Assisted private val savedState: SavedStateHandle,
    @Assisted private val submissionType: SrsSubmissionType,
    checkInRepository: CheckInRepository,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider) {

    private val selectedSetFlow = MutableStateFlow(initialSet())

    val checkIns: LiveData<List<CheckInsConsentItem>> = combine(
        checkInRepository.completedCheckIns,
        selectedSetFlow
    ) { checkIns, ids ->
        mutableListOf<CheckInsConsentItem>().apply {
            add(headerItem(checkIns))
            addAll(mapCheckIns(checkIns, ids))
        }
    }.asLiveData2()

    val events = SingleLiveEvent<SrsCheckinsNavigation>()

    fun onCloseClick() {
        Timber.d("onCancelClick")
        events.postValue(SrsCheckinsNavigation.ShowCloseDialog)
    }

    fun goHome() {
        Timber.d("goHome")
        events.postValue(SrsCheckinsNavigation.GoToHome)
    }

    fun onSkipClick() {
        Timber.d("onSkipClick")
        events.postValue(SrsCheckinsNavigation.ShowSkipDialog)
    }

    fun onNextClick() {
        Timber.d("onNextClick")
        val idsWithConsent = selectedSetFlow.value
        events.postValue(SrsCheckinsNavigation.GoToSymptomSubmission(submissionType, idsWithConsent.toLongArray()))
    }

    fun doNotShareCheckIns() {
        Timber.d("doNotShareCheckins")
        events.postValue(SrsCheckinsNavigation.GoToSymptomSubmission(submissionType))
    }

    private fun headerItem(checkIns: List<CheckIn>) = HeaderCheckInsVH.Item(
        selectAll = {
            val ids = checkIns.map { it.id }
            if (!selectedSetFlow.value.containsAll(ids)) {
                selectedSetFlow.value = updateSet(ids)
            }
        }
    )

    private fun mapCheckIns(checkIns: List<CheckIn>, ids: Set<Long>): List<CheckInsConsentItem> =
        checkIns.sortedByDescending { it.checkInEnd }
            .map { checkIn ->
                SelectableCheckInVH.Item(
                    checkIn = checkIn.copy(hasSubmissionConsent = ids.contains(checkIn.id)),
                    onItemSelected = { selectedSetFlow.value = updateSet(listOf(it.id)) }
                )
            }

    private fun updateSet(ids: List<Long>) =
        mutableSetOf<Long>().apply {
            if (!selectedSetFlow.value.containsAll(ids)) {
                addAll(ids) // New Ids
                addAll(selectedSetFlow.value) // Existing Ids
            } else {
                addAll(
                    selectedSetFlow.value.toMutableSet().apply { removeAll(ids.toSet()) }
                )
            }
        }.also {
            savedState[SET_KEY] = it
            Timber.d("SelectedCheckIns=$it")
        }

    private fun initialSet(): Set<Long> = savedState[SET_KEY] ?: emptySet()

    @AssistedFactory
    interface Factory : CWAViewModelFactory<SrsCheckinsFragmentViewModel> {

        fun create(
            savedState: SavedStateHandle,
            submissionType: SrsSubmissionType
        ): SrsCheckinsFragmentViewModel
    }

    companion object {
        private const val SET_KEY = "selected_checkIn_set"
    }
}
