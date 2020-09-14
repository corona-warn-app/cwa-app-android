package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomCalendarBinding
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.main.MainFragmentDirections
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel

class SubmissionSymptomCalendarFragment : Fragment() {

    private var _binding: FragmentSubmissionSymptomCalendarBinding? = null
    private val binding: FragmentSubmissionSymptomCalendarBinding get() = _binding!!
    private val submissionViewModel: SubmissionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSubmissionSymptomCalendarBinding.inflate(inflater)
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

        submissionViewModel.symptomCalendarEvent.observe(viewLifecycleOwner, Observer {
            when (it) {
                is SymptomCalendarEvent.NavigateToNext -> navigateToSymptomFinish()
                is SymptomCalendarEvent.NavigateToPrevious-> navigateToPreviousScreen()
            }
        })
    }

    private fun navigateToSymptomFinish() {
        // TODO: Place here the route to the next fragment
    }

    private fun navigateToPreviousScreen() {
        findNavController().doNavigate(SubmissionSymptomCalendarFragmentDirections
            .actionSubmissionCalendarFragmentToSubmissionSymptomIntroductionFragment())
    }

    private fun setButtonOnClickListener() {
        binding
            .submissionSymptomCalendarHeader.headerButtonBack.buttonIcon
            .setOnClickListener { submissionViewModel.onCalendarPreviousClicked() }

        binding
            .symptomButtonNext
            .setOnClickListener { submissionViewModel.onCalendarNextClicked() }
    }
}
