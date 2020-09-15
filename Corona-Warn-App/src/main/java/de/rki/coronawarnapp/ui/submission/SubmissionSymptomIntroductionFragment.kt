package de.rki.coronawarnapp.ui.submission

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.submission.SymptomIndication
import de.rki.coronawarnapp.ui.doNavigate
import de.rki.coronawarnapp.ui.viewmodel.SubmissionViewModel
import de.rki.coronawarnapp.util.formatter.formatButtonStyleByState

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
                is SymptomIntroductionEvent.SelectPositive-> selectPositiveButton()
            }
        })

        submissionViewModel.symptomIndication.observe(viewLifecycleOwner, Observer {
            updateButtons(it)
        })
    }

    private fun updateButtons(symptomIndication: SymptomIndication?){
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_apply).
            setTextColor(formatButtonStyleByState(symptomIndication, SymptomIndication.POSITIVE))
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_reject).
            setTextColor(formatButtonStyleByState(symptomIndication, SymptomIndication.NEGATIVE))
        binding.submissionSymptomContainer.findViewById<Button>(R.id.target_button_verify).
            setTextColor(formatButtonStyleByState(symptomIndication, SymptomIndication.NO_INFORMATION))
        // TODO disable continue button if symptomIndication == null
    }

    private fun navigateToSymptomCalendar() {
        findNavController().doNavigate(SubmissionSymptomIntroductionFragmentDirections
            .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment())
    }

    private fun navigateToPreviousScreen() {
        // TODO: Place here the route to the previous fragment
    }

    private fun selectPositiveButton()
    {
        submissionViewModel.onPositiveSymptomIndication()
    }

    private fun setButtonOnClickListener() {
        binding
            .submissionSymptomHeader.headerButtonBack.buttonIcon
            .setOnClickListener { submissionViewModel.onPreviousClicked() }

        binding
            .symptomButtonNext
            .setOnClickListener { submissionViewModel.onNextClicked() }

        binding
            .symptomChoiceSelection.targetButtonApply
            .setOnClickListener { submissionViewModel.onPositiveSymptomIndication() }
    }
}
