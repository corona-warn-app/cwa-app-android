package de.rki.coronawarnapp.contactdiary.ui.day

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.ContactDiaryDayTab
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

class ContactDiaryDayViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted selectedDay: Long
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val dateFormat by lazy {
        DateTimeFormat.forPattern("EEEE, dd.MM.yy")
    }

    private val activeInstant = Instant.ofEpochSecond(selectedDay)

    val contactDiaryTabs = listOf(ContactDiaryDayTab.PERSON_TAB, ContactDiaryDayTab.LOCATION_TAB)

    private val currentTab = MutableStateFlow(contactDiaryTabs[0])
    private val displayedDay = MutableStateFlow(activeInstant)

    val createPerson = SingleLiveEvent<Unit>()
    val createLocation = SingleLiveEvent<Unit>()

    val uiState = currentTab.combine(displayedDay) { currentTab, day ->
        UIState(
            fabTextResource = currentTab.fabTextResource,
            dayText = day.toString(dateFormat)
        )
    }.asLiveData()

    fun updateCurrentTab(position: Int) {
        currentTab.value = contactDiaryTabs[position]
    }

    fun onCreateButtonClicked() {
        launch {
            when (currentTab.value) {
                ContactDiaryDayTab.LOCATION_TAB -> createLocation.postValue(null)
                ContactDiaryDayTab.PERSON_TAB -> createPerson.postValue(null)
            }
        }
    }

    data class UIState(
        val fabTextResource: Int,
        val dayText: String
    )

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryDayViewModel> {
        fun create(selectedDay: Long): ContactDiaryDayViewModel
    }
}
