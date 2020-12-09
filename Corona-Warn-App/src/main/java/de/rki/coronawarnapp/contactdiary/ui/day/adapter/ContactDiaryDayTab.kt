package de.rki.coronawarnapp.contactdiary.ui.day.adapter

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.contactdiary.ui.day.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.person.ContactDiaryPersonListFragmentArgs
import de.rki.coronawarnapp.contactdiary.ui.day.place.ContactDiaryPlaceListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.place.ContactDiaryPlaceListFragmentArgs

data class ContactDiaryDayTab(
    val tabName: String,
    val fabText: String,
    val fragmentInstantiation: (day: Long) -> Fragment
) {
    companion object {
        val PERSON_TAB = ContactDiaryDayTab("Persons", "Person") { day ->
            ContactDiaryPersonListFragment().apply {
                // Feels kind of hacky but i like the free typesafety for the args
                arguments = ContactDiaryPersonListFragmentArgs(day).toBundle()
            }
        }
        val PLACE_TAB = ContactDiaryDayTab("Places", "Place") { day ->
            ContactDiaryPlaceListFragment().apply {
                // Feels kind of hacky but i like the free typesafety for the args
                arguments = ContactDiaryPlaceListFragmentArgs(day).toBundle()
            }
        }
    }
}
