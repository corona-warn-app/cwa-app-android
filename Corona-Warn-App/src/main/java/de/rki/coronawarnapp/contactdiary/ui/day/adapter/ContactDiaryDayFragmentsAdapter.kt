package de.rki.coronawarnapp.contactdiary.ui.day.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ContactDiaryDayFragmentsAdapter(
    fragment: Fragment,
    private val tabs: List<ContactDiaryDayTab>
) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = tabs.size

    override fun createFragment(position: Int): Fragment = tabs[position].fragmentInstantiation()
}
