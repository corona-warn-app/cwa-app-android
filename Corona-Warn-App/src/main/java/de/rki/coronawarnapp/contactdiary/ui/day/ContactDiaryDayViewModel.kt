package de.rki.coronawarnapp.contactdiary.ui.day

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.storage.ContactDiaryRepository
import de.rki.coronawarnapp.contactdiary.ui.day.adapter.ContactDiaryDayTab
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

class ContactDiaryDayViewModel @AssistedInject constructor(
    @Assisted selectedDay: Long,
    private val contactDiaryRepository: ContactDiaryRepository
) : CWAViewModel() {
    private val dateFormat by lazy {
        DateTimeFormat.forPattern("EEEE, dd.MM.yy")
    }

    val contactDiaryTabs = listOf(ContactDiaryDayTab.PERSON_TAB, ContactDiaryDayTab.LOCATION_TAB)

    private val currentTab = MutableStateFlow(contactDiaryTabs[0])
    private val displayedDay = MutableStateFlow(Instant.ofEpochSecond(selectedDay))

    val uiState = currentTab.combine(displayedDay) { currentTab, day ->
        UIState(
            fabText = currentTab.fabText,
            dayText = day.toString(dateFormat)
        )
    }.asLiveData()

    fun updateCurrentTab(position: Int) {
        currentTab.value = contactDiaryTabs[position]
    }

    fun onCreateButtonClicked() {
        //TODO replace with actual implementation
        launch {
            when (currentTab.value) {
                ContactDiaryDayTab.LOCATION_TAB -> contactDiaryRepository.addDummyLocation()
                ContactDiaryDayTab.PERSON_TAB -> contactDiaryRepository.addDummyPerson()
            }
        }
    }

    data class UIState(
        val fabText: String,
        val dayText: String
    )

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryDayViewModel> {
        fun create(selectedDay: Long): ContactDiaryDayViewModel
    }
}
