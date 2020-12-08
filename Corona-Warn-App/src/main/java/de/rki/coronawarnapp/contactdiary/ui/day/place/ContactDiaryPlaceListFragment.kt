package de.rki.coronawarnapp.contactdiary.ui.day.place

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryPlaceListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class ContactDiaryPlaceListFragment : Fragment(R.layout.contact_diary_place_list_fragment), AutoInject {
    private val binding: ContactDiaryPlaceListFragmentBinding by viewBindingLazy()
}
