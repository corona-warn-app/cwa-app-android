package de.rki.coronawarnapp.contactdiary.ui.day.adapter

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.contactdiary.ui.day.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.place.ContactDiaryPlaceListFragment

data class ContactDiaryDayTab(val tabName: String, val fabText: String, val fragmentInstantiation: () -> Fragment) {
    companion object {
        val PERSON_TAB = ContactDiaryDayTab("Persons", "Person") { ContactDiaryPersonListFragment() }
        val PLACE_TAB = ContactDiaryDayTab("Places", "Place") { ContactDiaryPlaceListFragment() }
    }
}
