package de.rki.coronawarnapp.contactdiary.ui.day.person

import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryPersonListFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class ContactDiaryPersonListFragment : Fragment(R.layout.contact_diary_person_list_fragment), AutoInject {
    private val binding: ContactDiaryPersonListFragmentBinding by viewBindingLazy()
}
