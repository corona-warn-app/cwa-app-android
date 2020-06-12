package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentSubmissionIntroBinding
import de.rki.coronawarnapp.ui.doNavigate

/**
 * The [SubmissionIntroFragment] displays information about how the corona warning system works
 */
class SubmissionIntroFragment : Fragment() {

    private var _binding: FragmentSubmissionIntroBinding? = null
    private val binding: FragmentSubmissionIntroBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        _binding = FragmentSubmissionIntroBinding.inflate(inflater)
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
        binding.submissionIntroRoot.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.submissionIntroHeader.headerButtonBack.buttonIcon.setOnClickListener {
            findNavController().doNavigate(
                SubmissionIntroFragmentDirections.actionSubmissionIntroFragmentToMainFragment()
            )
        }
        binding.submissionIntroButtonNext.setOnClickListener {
            findNavController().doNavigate(
                SubmissionIntroFragmentDirections.actionSubmissionIntroFragmentToSubmissionDispatcherFragment()
            )
        }
    }
}
