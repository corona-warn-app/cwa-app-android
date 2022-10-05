package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import androidx.lifecycle.asLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.model.toEditableVariant
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.trimToLength
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.Collator
import java.time.Duration
import java.time.LocalDate

class ContactDiaryLocationListViewModel @AssistedInject constructor(
    val dispatcherProvider: DispatcherProvider,
    @AppScope val appScope: CoroutineScope,
    @Assisted selectedDay: String,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, ex ->
        ex.report(ExceptionCategory.INTERNAL, TAG)
    }

    val openCommentInfo = SingleLiveEvent<Unit>()
    val openDialog = SingleLiveEvent<String>()
    private var currentLocation: DiaryLocationListItem? = null

    private val localDate = LocalDate.parse(selectedDay)

    private val dayElement = contactDiaryRepository.locationVisitsForDate(localDate)
    private val selectableLocations = contactDiaryRepository.locations

    private val diaryLocationListItems: Flow<List<DiaryLocationListItem>> =
        selectableLocations.combine(dayElement) { locations, encounters ->
            locations.sortedWith(
                compareBy(Collator.getInstance()) { it.locationName }
            ).map { location ->
                val visit = encounters.singleOrNull {
                    it.contactDiaryLocation.locationId == location.locationId
                }
                DiaryLocationListItem(
                    item = location,
                    visit = visit,
                    onItemClick = { onLocationSelectionChanged(it.stableId) },
                    onDurationChanged = { item, duration ->
                        onDurationChanged(item, duration)
                    },
                    onCircumstancesChanged = { item, circumstances ->
                        onCircumstancesChanged(item, circumstances)
                    },
                    onCircumStanceInfoClicked = {
                        openCommentInfo.postValue(Unit)
                    },
                    onDurationDialog = { item, durationString ->
                        onDurationDialog(item, durationString)
                    }
                )
            }
        }

    val uiList = diaryLocationListItems.asLiveData(dispatcherProvider.Default)

    private fun onLocationSelectionChanged(itemId: Long) = launchOnAppScope {
        diaryLocationListItems.first().find { it.stableId == itemId }?.let { item ->
            if (!item.selected) {
                contactDiaryRepository.addLocationVisit(
                    DefaultContactDiaryLocationVisit(
                        date = localDate,
                        contactDiaryLocation = item.item
                    )
                )
            } else {
                val visit = dayElement
                    .first()
                    .find { it.contactDiaryLocation.locationId == item.item.locationId }
                visit?.let { contactDiaryRepository.deleteLocationVisit(it) }
            }
        } ?: run { Timber.d("No item found for id $itemId") }
    }

    private fun onDurationDialog(
        listItem: DiaryLocationListItem,
        durationString: String
    ) {
        currentLocation = listItem
        openDialog.postValue(durationString)
    }

    fun onDurationSelected(duration: Duration) {
        currentLocation?.let { onDurationChanged(it, duration) }
    }

    private fun onDurationChanged(
        item: DiaryLocationListItem,
        duration: Duration?
    ) {
        val visit = item.visit ?: return
        launchOnAppScope {
            contactDiaryRepository.updateLocationVisit(visit.id) {
                it.toEditableVariant().copy(duration = duration)
            }
        }
    }

    private fun onCircumstancesChanged(
        item: DiaryLocationListItem,
        circumstances: String
    ) {
        val visit = item.visit ?: return

        if (visit.circumstances.isNullOrBlank() && circumstances.isBlank()) {
            Timber.d("onCircumstancesChanged but there is nothing to be updated")
            return
        }

        val sanitized = circumstances.trim().trimToLength(250)
        launchOnAppScope {
            contactDiaryRepository.updateLocationVisit(visit.id) {
                it.toEditableVariant().copy(circumstances = sanitized)
            }
        }
    }

    // Viewmodel may be cancelled before the data is saved
    private fun launchOnAppScope(block: suspend CoroutineScope.() -> Unit) =
        appScope.launch(coroutineExceptionHandler) {
            block()
        }

    @AssistedFactory
    interface Factory : CWAViewModelFactory<ContactDiaryLocationListViewModel> {
        fun create(selectedDay: String): ContactDiaryLocationListViewModel
    }
}

private val TAG = ContactDiaryLocationListViewModel::class.java.simpleName
