package de.rki.coronawarnapp.contactdiary.ui.day

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.ContactDiaryDayTab
import de.rki.coronawarnapp.contactdiary.util.toFormattedDay
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.joda.time.LocalDate

class ContactDiaryDayViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    @Assisted selectedDay: String
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {
    private val displayedDay = MutableStateFlow(LocalDate.parse(selectedDay))

    val routeToScreen: SingleLiveEvent<ContactDiaryDayNavigationEvents> = SingleLiveEvent()

    val uiState = displayedDay.map { day ->
        UIState(dayText = day.toFormattedDay())
    }.asLiveData()

    fun onCreateButtonClicked(activeTab: ContactDiaryDayTab) {
        when (activeTab) {
            ContactDiaryDayTab.LocationTab -> routeToScreen
                .postValue(ContactDiaryDayNavigationEvents.NavigateToAddLocationBottomSheet)
            ContactDiaryDayTab.PersonTab -> routeToScreen
                .postValue(ContactDiaryDayNavigationEvents.NavigateToAddPersonBottomSheet)
        }
    }

    fun onBackPressed() {
        routeToScreen.postValue(ContactDiaryDayNavigationEvents.NavigateToOverviewFragment)
    }

    data class UIState(
        val dayText: String
    )

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryDayViewModel> {
        fun create(selectedDay: String): ContactDiaryDayViewModel
    }
}
