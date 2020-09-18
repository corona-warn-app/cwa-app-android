package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBindingLazy

/**
 * The [SubmissionDoneFragment] displays information to a user that submitted his exposure keys
 */
class SubmissionDoneFragment : Fragment(R.layout.fragment_submission_done) {

    private val binding: FragmentSubmissionDoneBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDoneContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionDoneHeader.headerButtonBack.buttonIcon.setOnClickListener {
            findNavController().doNavigate(
                SubmissionDoneFragmentDirections.actionSubmissionDoneFragmentToMainFragment()
            )
        }
        binding.submissionDoneButtonDone.setOnClickListener {
            findNavController().doNavigate(
                SubmissionDoneFragmentDirections.actionSubmissionDoneFragmentToMainFragment()
            )
        }
    }
}
