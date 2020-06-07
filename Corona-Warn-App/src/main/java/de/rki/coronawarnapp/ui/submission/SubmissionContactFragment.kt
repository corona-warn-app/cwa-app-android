package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionContactBinding
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.CallHelper
import kotlinx.android.synthetic.main.fragment_submission_contact.*

/**
 * The [SubmissionContactFragment] allows requesting a teletan via phone
 */
class SubmissionContactFragment : BaseFragment() {

    private var _binding: FragmentSubmissionContactBinding? = null
    private val binding: FragmentSubmissionContactBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // get the binding reference by inflating it with the current layout
        _binding = FragmentSubmissionContactBinding.inflate(inflater)
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

    override fun onStart() {
        super.onStart()
        submission_contact_scrollview.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    override fun onResume() {
        super.onResume()
        submission_contact_scrollview.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setButtonOnClickListener() {
        binding.submissionContactHeader.headerToolbar.setNavigationOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.submissionContactButtonCall.setOnClickListener {
            dial()
        }
        binding.submissionContactButtonEnter.setOnClickListener {
            doNavigate(SubmissionContactFragmentDirections.actionSubmissionContactFragmentToSubmissionTanFragment())
        }
    }

    private fun dial() = context?.let {
        val number = getString(R.string.submission_contact_number_dial)
        CallHelper.call(this, "tel:$number")
    }
}
