package de.rki.coronawarnapp.contactdiary.ui.day.tabs.person

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryPersonCommentInfoFragmentBinding
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class ContactDiaryPersonCommentInfoFragment : Fragment(R.layout.contact_diary_person_comment_info_fragment) {

    private val binding: ContactDiaryPersonCommentInfoFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }
    }
}
