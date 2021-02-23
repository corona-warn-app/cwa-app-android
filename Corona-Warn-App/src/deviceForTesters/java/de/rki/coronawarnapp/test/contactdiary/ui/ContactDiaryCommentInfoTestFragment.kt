package de.rki.coronawarnapp.test.contactdiary.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryCommentInfoFragmentBinding
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy

class ContactDiaryCommentInfoTestFragment : Fragment(R.layout.contact_diary_comment_info_fragment) {

    private val binding: ContactDiaryCommentInfoFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }
    }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Contact Diary Comment Info",
            description = "Contact diary comment info screen",
            targetId = R.id.test_contact_diary_comment_fragment
        )
    }
}
