package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.rki.coronawarnapp.databinding.FragmentSubmissionIntroBinding
import de.rki.coronawarnapp.ui.BaseFragment

/**
 * The [SubmissionIntroFragment] displays information about how the corona warning system works
 */
class SubmissionIntroFragment : BaseFragment() {

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

    private fun setButtonOnClickListener() {
        binding.submissionIntroHeader.headerToolbar.setNavigationOnClickListener {
            doNavigate(SubmissionIntroFragmentDirections.actionSubmissionIntroFragmentToMainFragment())
        }
        binding.submissionIntroButtonNext.setOnClickListener {
            doNavigate(SubmissionIntroFragmentDirections.actionSubmissionIntroFragmentToSubmissionDispatcherFragment())
        }
    }
}
