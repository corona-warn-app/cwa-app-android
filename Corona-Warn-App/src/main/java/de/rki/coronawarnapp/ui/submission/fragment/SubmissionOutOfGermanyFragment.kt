package de.rki.coronawarnapp.ui.submission.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import de.rki.coronawarnapp.databinding.FragmentSubmissionOutofgermanySelectionBinding
import de.rki.coronawarnapp.ui.submission.viewmodel.ButtonClicked
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionOutOfGermanyViewModel

class SubmissionOutOfGermanyFragment : Fragment() {

    private val viewModel: SubmissionOutOfGermanyViewModel by viewModels()
    private var _binding: FragmentSubmissionOutofgermanySelectionBinding? = null
    private val binding: FragmentSubmissionOutofgermanySelectionBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionOutofgermanySelectionBinding.inflate(inflater)
        binding.submissionOutOfGermanyViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.submissionOutofgermanyPositiveSelection.submissionOutofgermanyContainer.setOnClickListener {
            viewModel.buttonClicked(ButtonClicked.YES)
        }

        binding.submissionOutofgermanyNegativeSelection.submissionOutofgermanyContainer.setOnClickListener {
            viewModel.buttonClicked(ButtonClicked.NO)
        }

        binding.submissionOutofgermanyNoSelection.submissionOutofgermanyContainer.setOnClickListener {
            viewModel.buttonClicked(ButtonClicked.NOT_SPECIFIED)
        }

    }

    companion object {
        private val TAG: String = SubmissionOutOfGermanyFragment::class.java.simpleName
    }
}
