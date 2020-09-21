package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionContactBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import kotlinx.android.synthetic.main.include_submission_contact.view.*

/**
 * The [SubmissionContactFragment] allows requesting a teletan via phone
 */
class SubmissionContactFragment : Fragment(R.layout.fragment_submission_contact) {

    private val binding: FragmentSubmissionContactBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.submissionContactRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionContactHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.submissionContactButtonCall.setOnClickListener {
            dial()
        }
        binding.submissionContactRoot.submission_contact_step_1_number.setOnClickListener {
            dial()
        }
        binding.submissionContactButtonEnter.setOnClickListener {
            findNavController().doNavigate(
                SubmissionContactFragmentDirections.actionSubmissionContactFragmentToSubmissionTanFragment()
            )
        }
    }

    private fun dial() = context?.let {
        val number = getString(R.string.submission_contact_number_dial)
        ExternalActionHelper.call(this, number)
    }
}
