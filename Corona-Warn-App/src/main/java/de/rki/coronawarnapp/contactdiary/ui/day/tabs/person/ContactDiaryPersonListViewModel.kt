package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.model.ContactDiaryPerson
import de.rki.coronawarnapp.contactdiary.model.DefaultContactDiaryPersonEncounter
import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.util.SelectableItem
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.joda.time.LocalDate

class ContactDiaryPersonListViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted selectedDay: String,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, ex ->
        ex.report(ExceptionCategory.INTERNAL, TAG)
    }

    private val localDate = LocalDate.parse(selectedDay)

    private val dayElement = contactDiaryRepository.personEncountersForDate(localDate)
    private val selectablePersons = contactDiaryRepository.people

    val uiList = selectablePersons.combine(dayElement) { persons, dayElement ->
        persons.map { contactDiaryPerson ->
            if (dayElement.any { it.contactDiaryPerson.personId == contactDiaryPerson.personId }) {
                SelectableItem(
                    true,
                    contactDiaryPerson,
                    SELECTED_CONTENT_DESCRIPTION.toResolvingString(contactDiaryPerson.fullName),
                    UNSELECTED_CONTENT_DESCRIPTION.toResolvingString(contactDiaryPerson.fullName),
                    DESELECT_ACTION_DESCRIPTION,
                    SELECT_ACTION_DESCRIPTION
                )
            } else {
                SelectableItem(
                    false,
                    contactDiaryPerson,
                    UNSELECTED_CONTENT_DESCRIPTION.toResolvingString(contactDiaryPerson.fullName),
                    SELECTED_CONTENT_DESCRIPTION.toResolvingString(contactDiaryPerson.fullName),
                    SELECT_ACTION_DESCRIPTION,
                    DESELECT_ACTION_DESCRIPTION
                )
            }
        }
    }.asLiveData()

    fun onPersonSelectionChanged(item: SelectableItem<ContactDiaryPerson>) = launch(coroutineExceptionHandler) {
        if (!item.selected) {
            contactDiaryRepository.addPersonEncounter(
                DefaultContactDiaryPersonEncounter(
                    date = localDate,
                    contactDiaryPerson = item.item
                )
            )
        } else {
            val visit = dayElement.first()
                .find { it.contactDiaryPerson.personId == item.item.personId }
            visit?.let { contactDiaryRepository.deletePersonEncounter(it) }
        }
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryPersonListViewModel> {
        fun create(selectedDay: String): ContactDiaryPersonListViewModel
    }
}

private val TAG = ContactDiaryPersonListViewModel::class.java.simpleName
private const val SELECTED_CONTENT_DESCRIPTION = R.string.accessibility_person_selected
private const val UNSELECTED_CONTENT_DESCRIPTION = R.string.accessibility_person_unselected
private const val SELECT_ACTION_DESCRIPTION = R.string.accessibility_action_select
private const val DESELECT_ACTION_DESCRIPTION = R.string.accessibility_action_deselect
