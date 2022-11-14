package de.rki.coronawarnapp.srs.ui.done

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

class SrsSubmissionDoneFragment : Fragment(R.layout.fragment_submission_done) {

    private val args by navArgs<SrsSubmissionDoneFragmentArgs>()

    private val binding: FragmentSubmissionDoneBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            toolbar.apply {
                navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                navigationContentDescription = getString(R.string.accessibility_close)
                setNavigationOnClickListener { popBackStack() }
            }

            submissionDoneButtonDone.setOnClickListener { popBackStack() }
            submissionDoneContent.submissionDoneContent.submissionDonePcrValidation.root.isVisible = true // TODO
            submissionDoneContent.submissionDoneContent.submissionDoneIllness.root.isVisible = true // TODO
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionDoneContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
