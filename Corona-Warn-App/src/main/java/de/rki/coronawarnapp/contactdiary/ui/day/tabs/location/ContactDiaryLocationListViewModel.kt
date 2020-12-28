package de.rki.coronawarnapp.contactdiary.ui.day.tabs.location

import android.content.Context
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryLocation
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryLocationVisit
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.util.SelectableItem
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.joda.time.LocalDate

class ContactDiaryLocationListViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted selectedDay: String,
    private val contactDiaryRepository: ContactDiaryRepository,
    @AppContext context: Context
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, ex ->
        ex.report(ExceptionCategory.INTERNAL, TAG)
    }

    private val localDate = LocalDate.parse(selectedDay)

    private val dayElement = contactDiaryRepository.locationVisitsForDate(localDate)
    private val selectableLocations = contactDiaryRepository.locations

    private val selectActionDescriptionString: String = context.getString(selectActionDescription)
    private val deselectActionDescriptionString: String = context.getString(deselectActionDescription)

    val uiList = selectableLocations.combine(dayElement) { locations, dayElement ->
        locations.map { contactDiaryLocation ->
            if (dayElement.any { it.contactDiaryLocation.locationId == contactDiaryLocation.locationId }) {
                SelectableItem(
                    true,
                    contactDiaryLocation,
                    context.getString(selectedContentDescription, contactDiaryLocation.locationName),
                    context.getString(unselectedContentDescription, contactDiaryLocation.locationName),
                    deselectActionDescriptionString,
                    selectActionDescriptionString
                )
            } else {
                SelectableItem(
                    false,
                    contactDiaryLocation,
                    context.getString(unselectedContentDescription, contactDiaryLocation.locationName),
                    context.getString(selectedContentDescription, contactDiaryLocation.locationName),
                    selectActionDescriptionString,
                    deselectActionDescriptionString
                )
            }
        }
    }.asLiveData()

    fun onLocationSelectionChanged(item: SelectableItem<ContactDiaryLocation>) = launch(coroutineExceptionHandler) {
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
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryLocationListViewModel> {
        fun create(selectedDay: String): ContactDiaryLocationListViewModel
    }
}

private val TAG = ContactDiaryLocationListViewModel::class.java.simpleName
private const val selectedContentDescription = R.string.accessibility_location_selected
private const val unselectedContentDescription = R.string.accessibility_location_unselected
private const val selectActionDescription = R.string.accessibility_action_select
private const val deselectActionDescription = R.string.accessibility_action_deselect
