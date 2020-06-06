package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.databinding.FragmentSubmissionDoneBinding
import de.rki.coronawarnapp.ui.BaseFragment

/**
 * The [SubmissionDoneFragment] displays information to a user that submitted his exposure keys
 */
class SubmissionDoneFragment : BaseFragment() {

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

    private fun setButtonOnClickListener() {
        binding.submissionDoneHeader.toolbar.setNavigationOnClickListener {
            doNavigate(
                SubmissionDoneFragmentDirections.actionSubmissionDoneFragmentToMainFragment()
            )
        }
        binding.submissionDoneButtonDone.setOnClickListener {
            doNavigate(
                SubmissionDoneFragmentDirections.actionSubmissionDoneFragmentToMainFragment()
            )
        }
    }
}
