package de.rki.coronawarnapp.contactdiary.ui.day.tabs

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryCommentInfoFragmentBinding
import de.rki.coronawarnapp.util.ui.addNavigationIconButtonId
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class ContactDiaryCommentInfoFragment : Fragment(R.layout.contact_diary_comment_info_fragment) {

    private val binding: ContactDiaryCommentInfoFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            popBackStack()
        }
        binding.toolbar.addNavigationIconButtonId(R.id.contact_diary_comment_info_fragment_navigation_icon_buttonId)
    }
}
