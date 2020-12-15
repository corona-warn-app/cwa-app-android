package de.rki.coronawarnapp.contactdiary.ui.day.tabs

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ContactDiaryDayFragmentsAdapter(
    fragment: Fragment,
    val tabs: List<ContactDiaryDayTab>,
    private val day: String
) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = tabs.size

    override fun createFragment(position: Int): Fragment = tabs[position].fragmentInstantiation(day)
}
