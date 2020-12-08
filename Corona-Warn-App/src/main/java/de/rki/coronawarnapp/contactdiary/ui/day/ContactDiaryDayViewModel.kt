package de.rki.coronawarnapp.contactdiary.ui.day

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.contactdiary.ui.day.adapter.ContactDiaryDayTab
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactory
import org.joda.time.Instant

class ContactDiaryDayViewModel @AssistedInject constructor(
    @Assisted private val selectedDay: Long,
) : CWAViewModel() {
    val contactDiaryTabs = listOf(ContactDiaryDayTab.PERSON_TAB, ContactDiaryDayTab.PLACE_TAB)

    private val _currentTab = MutableLiveData(ContactDiaryDayTab.PERSON_TAB)
    val currentTab: LiveData<ContactDiaryDayTab> = _currentTab

    private val _displayedDay = MutableLiveData(Instant.ofEpochSecond(selectedDay))
    val displayedDay: LiveData<Instant> = _displayedDay

    fun updateCurrentTab(position: Int) {
        _currentTab.postValue(contactDiaryTabs[position])
    }

    fun onCreateButtonClicked() {
        TODO("Not yet implemented")
    }

    @AssistedInject.Factory
    interface Factory : CWAViewModelFactory<ContactDiaryDayViewModel> {
        fun create(selectedDay: Long): ContactDiaryDayViewModel
    }
}
