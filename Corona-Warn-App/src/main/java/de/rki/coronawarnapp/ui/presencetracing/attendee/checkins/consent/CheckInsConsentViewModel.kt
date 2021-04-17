package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import timber.log.Timber

class CheckInsConsentViewModel @AssistedInject constructor(
    @Assisted private val savedState: SavedStateHandle,
    dispatcherProvider: DispatcherProvider,
    checkInRepository: CheckInRepository,
) : CWAViewModel(dispatcherProvider) {

    private val selectedSetFlow = MutableStateFlow(initialSet())

    val checkIns: LiveData<List<CheckInsConsentItem>> = combine(
        checkInRepository.checkInsWithinRetention,
        selectedSetFlow
    ) { checkIns, ids ->
        mutableListOf<CheckInsConsentItem>().apply {
            add(headerItem(checkIns))
            addAll(mapCheckIns(checkIns, ids))
        }
    }.asLiveData(context = dispatcherProvider.Default)

    private fun headerItem(checkIns: List<CheckIn>) = HeaderCheckInsVH.Item(
        selectAll = { selectedSetFlow.value = updateSet(checkIns) }
    )

    private fun mapCheckIns(checkIns: List<CheckIn>, ids: Set<Long>): List<CheckInsConsentItem> =
        checkIns.filter { it.completed }
            .sortedByDescending { it.checkInEnd }
            .map { checkIn ->
                SelectableCheckInVH.Item(
                    checkIn = checkIn.copy(isSubmissionPermitted = ids.contains(checkIn.id)),
                    onItemSelected = { selectedSetFlow.value = updateSet(listOf(it)) }
                )
            }

    private fun updateSet(checkIns: List<CheckIn>) =
        mutableSetOf<Long>().apply {
            val ids = checkIns.map { it.id }
            if (!selectedSetFlow.value.containsAll(ids)) {
                addAll(ids) // New Ids
                addAll(selectedSetFlow.value) // Existing Ids
            } else {
                addAll(
                    selectedSetFlow.value.toMutableSet().apply { removeAll(ids) }
                )
            }
        }.also {
            savedState.set(SET_KEY, it)
            Timber.d("SelectedCheckIns=$it")
        }

    private fun initialSet(): Set<Long> = savedState.get(SET_KEY) ?: emptySet()

    @AssistedFactory
    interface Factory : CWAViewModelFactory<CheckInsConsentViewModel> {
        fun create(
            savedState: SavedStateHandle,
        ): CheckInsConsentViewModel
    }

    companion object {
        private const val SET_KEY = "selected_checkIn_set"
    }
}
