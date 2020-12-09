package de.rki.coronawarnapp.contactdiary.ui.day.adapter

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.contactdiary.ui.day.location.ContactDiaryLocationListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.location.ContactDiaryLocationListFragmentArgs
import de.rki.coronawarnapp.contactdiary.ui.day.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.person.ContactDiaryPersonListFragmentArgs

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
        val LOCATION_TAB = ContactDiaryDayTab("Places", "Place") { day ->
            ContactDiaryLocationListFragment().apply {
                // Feels kind of hacky but i like the free typesafety for the args
                arguments = ContactDiaryLocationListFragmentArgs(day).toBundle()
            }
        }
    }
}
