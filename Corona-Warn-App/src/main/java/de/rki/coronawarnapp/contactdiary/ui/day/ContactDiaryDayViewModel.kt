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
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

class ContactDiaryDayViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted selectedDay: String
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val dateFormat by lazy {
        DateTimeFormat.forPattern("EEEE, dd.MM.yy")
    }

    private val displayedDay = MutableStateFlow(LocalDate.parse(selectedDay))

    val createPerson = SingleLiveEvent<Unit>()
    val createLocation = SingleLiveEvent<Unit>()

    val uiState = displayedDay.map { day ->
        UIState(dayText = day.toString(dateFormat))
    }.asLiveData()

    fun onCreateButtonClicked(activeTab: ContactDiaryDayTab) {
        when (activeTab) {
            ContactDiaryDayTab.LOCATION_TAB -> createLocation.postValue(null)
            ContactDiaryDayTab.PERSON_TAB -> createPerson.postValue(null)
        }
    }

    data class UIState(
        val dayText: String
    )

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryDayViewModel> {
        fun create(selectedDay: String): ContactDiaryDayViewModel
    }
}
