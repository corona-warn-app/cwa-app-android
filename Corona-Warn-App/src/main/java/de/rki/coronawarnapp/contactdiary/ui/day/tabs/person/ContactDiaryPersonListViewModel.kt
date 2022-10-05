package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.model.toEditableVariant
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.flow.combine
import de.rki.coronawarnapp.util.trimToLength
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.Collator
import java.time.LocalDate

class ContactDiaryPersonListViewModel @AssistedInject constructor(
    val dispatcherProvider: DispatcherProvider,
    @AppScope val appScope: CoroutineScope,
    @Assisted selectedDay: String,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, ex ->
        ex.report(ExceptionCategory.INTERNAL, TAG)
    }

    val openCommentInfo = SingleLiveEvent<Unit>()

    private val localDate = LocalDate.parse(selectedDay)

    private val dayEncounters = contactDiaryRepository.personEncountersForDate(localDate)
    private val selectablePersons = contactDiaryRepository.people

    private val diaryPersonListItems: Flow<List<DiaryPersonListItem>> = combine(
        selectablePersons,
        dayEncounters
    ) { persons, encounters ->
        persons.sortedWith(
            compareBy(Collator.getInstance()) { it.fullName }
        ).map { person ->
            val encounter = encounters.singleOrNull {
                it.contactDiaryPerson.personId == person.personId
            }
            DiaryPersonListItem(
                item = person,
                personEncounter = encounter,
                onItemClick = { onPersonSelectionChanged(it.stableId) },
                onDurationChanged = { item, duration ->
                    onDurationChanged(item, duration)
                },
                onWasOutsideChanged = { item, wasOutside ->
                    onWasOutsideChanged(item, wasOutside)
                },
                onWithMaskChanged = { item, withMask ->
                    onWithmaskChanged(item, withMask)
                },
                onCircumstancesChanged = { item, circumstances ->
                    onCircumstancesChanged(item, circumstances)
                },
                onCircumstanceInfoClicked = {
                    openCommentInfo.postValue(Unit)
                }
            )
        }
    }

    val uiList: LiveData<List<DiaryPersonListItem>> = diaryPersonListItems
        .asLiveData(context = dispatcherProvider.Default)

    private fun onPersonSelectionChanged(
        itemId: Long
    ) = launchOnAppScope {
        diaryPersonListItems.first().find { it.stableId == itemId }?.let { item ->
            if (!item.selected) {
                contactDiaryRepository.addPersonEncounter(
                    DefaultContactDiaryPersonEncounter(
                        date = localDate,
                        contactDiaryPerson = item.item
                    )
                )
            } else {
                val visit = dayEncounters.first()
                    .find { it.contactDiaryPerson.personId == item.item.personId }
                visit?.let { contactDiaryRepository.deletePersonEncounter(it) }
            }
        } ?: run { Timber.d("No item found for id $itemId") }
    }

    private fun onDurationChanged(
        item: DiaryPersonListItem,
        duration: ContactDiaryPersonEncounter.DurationClassification?
    ) {
        Timber.d("onDurationChanged(item=%s, duration=%s)", item, duration)
        val encounter = item.personEncounter ?: return
        launchOnAppScope {
            contactDiaryRepository.updatePersonEncounter(encounter.id) {
                it.toEditableVariant().copy(durationClassification = duration)
            }
        }
    }

    // Viewmodel may be cancelled before the data is saved
    private fun launchOnAppScope(block: suspend CoroutineScope.() -> Unit) =
        appScope.launch(coroutineExceptionHandler) {
            block()
        }

    private fun onWithmaskChanged(
        item: DiaryPersonListItem,
        withMask: Boolean?
    ) {
        Timber.d("onWithmaskChanged(item=%s, withMask=%s)", item, withMask)
        val encounter = item.personEncounter ?: return
        launchOnAppScope {
            contactDiaryRepository.updatePersonEncounter(encounter.id) {
                it.toEditableVariant().copy(withMask = withMask)
            }
        }
    }

    private fun onWasOutsideChanged(
        item: DiaryPersonListItem,
        wasOutside: Boolean?
    ) {
        Timber.d("onWasOutsideChanged(item=%s, onWasOutside=%s)", item, wasOutside)
        val encounter = item.personEncounter ?: return
        launchOnAppScope {
            contactDiaryRepository.updatePersonEncounter(encounter.id) {
                it.toEditableVariant().copy(wasOutside = wasOutside)
            }
        }
    }

    private fun onCircumstancesChanged(
        item: DiaryPersonListItem,
        circumstances: String
    ) {
        Timber.d("onCircumstancesChanged(item=%s, circumstances=%s)", item, circumstances)
        val encounter = item.personEncounter ?: return

        if (encounter.circumstances.isNullOrBlank() && circumstances.isBlank()) {
            Timber.d("onCircumstancesChanged but there is nothing to be updated")
            return
        }

        launchOnAppScope {
            val sanitized = circumstances.trim().trimToLength(250)
            contactDiaryRepository.updatePersonEncounter(encounter.id) {
                it.toEditableVariant().copy(circumstances = sanitized)
            }
        }
    }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ContactDiaryPersonListViewModel> {
        fun create(selectedDay: String): ContactDiaryPersonListViewModel
    }
}

private val TAG = ContactDiaryPersonListViewModel::class.java.simpleName
