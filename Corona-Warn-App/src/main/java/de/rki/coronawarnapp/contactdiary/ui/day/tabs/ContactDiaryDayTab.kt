package de.rki.coronawarnapp.contactdiary.ui.day.tabs

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.location.ContactDiaryLocationListFragmentArgs
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListFragment
import de.rki.coronawarnapp.contactdiary.ui.day.tabs.person.ContactDiaryPersonListFragmentArgs

sealed class ContactDiaryDayTab(
    val tabNameResource: Int,
    val fabTextResource: Int,
    val fragmentInstantiation: (day: String) -> Fragment
) {
    object PersonTab : ContactDiaryDayTab(
        R.string.contact_diary_day_person_tab_title,
        R.string.contact_diary_day_person_fab_title,
        { day ->
            ContactDiaryPersonListFragment().apply {
                // Feels kind of hacky but i like the free typesafety for the args
                arguments = ContactDiaryPersonListFragmentArgs(day).toBundle()
            }
        }
    )

    object LocationTab : ContactDiaryDayTab(
        R.string.contact_diary_day_location_tab_title,
        R.string.contact_diary_day_location_fab_title,
        { day ->
            ContactDiaryLocationListFragment().apply {
                // Feels kind of hacky but i like the free typesafety for the args
                arguments = ContactDiaryLocationListFragmentArgs(day).toBundle()
            }
        }
    )
}
