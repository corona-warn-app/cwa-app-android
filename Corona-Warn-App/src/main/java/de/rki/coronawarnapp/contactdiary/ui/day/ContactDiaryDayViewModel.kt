package de.rki.coronawarnapp.contactdiary.ui.day

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.day.adapter.ContactDiaryDayTab
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

class ContactDiaryDayViewModel @AssistedInject constructor(
    @Assisted private val selectedDay: Long,
) : CWAViewModel() {
    private val dateFormat by lazy {
        DateTimeFormat.forPattern("EEEE, dd.MM.yy")
    }

    val contactDiaryTabs = listOf(ContactDiaryDayTab.PERSON_TAB, ContactDiaryDayTab.PLACE_TAB)

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
        TODO("Not yet implemented")
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

