package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneBinding
import de.rki.coronawarnapp.ui.doNavigate

/**
 * The [SubmissionDoneFragment] displays information to a user that submitted his exposure keys
 */
class SubmissionDoneFragment : Fragment() {

    private var _binding: FragmentSubmissionDoneBinding? = null
    private val binding: FragmentSubmissionDoneBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        _binding = FragmentSubmissionDoneBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
