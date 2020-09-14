package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel

class SubmissionSymptomIntroductionFragment : Fragment() {

    private var _binding: FragmentSubmissionSymptomIntroBinding? = null
    private val binding: FragmentSubmissionSymptomIntroBinding get() = _binding!!
    private val submissionViewModel: SubmissionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionSymptomIntroBinding.inflate(inflater)
        binding.submissionViewModel = submissionViewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()

        submissionViewModel.symptomIntroductionEvent.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SymptomIntroductionEvent.NavigateToSymptomCalendar -> navigateToSymptomCalendar()
                is SymptomIntroductionEvent.NavigateToPreviousScreen -> navigateToPreviousScreen()
            }
        })
    }

    private fun navigateToSymptomCalendar() {
        findNavController().doNavigate(SubmissionSymptomIntroductionFragmentDirections
            .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment())
    }

    private fun navigateToPreviousScreen() {
        // TODO: Place here the route to the previous fragment
    }

    private fun setButtonOnClickListener() {
        binding
            .submissionSymptomHeader.headerButtonBack.buttonIcon
            .setOnClickListener { submissionViewModel.onPreviousClicked() }

        binding
            .symptomButtonNext
            .setOnClickListener { submissionViewModel.onNextClicked() }
    }
}
