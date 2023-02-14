package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.submissionCancelDialog
import de.rki.coronawarnapp.util.formatter.formatSymptomBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatSymptomButtonTextStyleByState
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * The [SubmissionSymptomIntroductionFragment], initial fragment displayed when the user starts the submission process
 * providing symptoms, asking whether or not the user has experienced any of the common symptoms of COVID-19.
 */
@AndroidEntryPoint
class SubmissionSymptomIntroductionFragment : Fragment(R.layout.fragment_submission_symptom_intro) {

    @Inject lateinit var factory: SubmissionSymptomIntroductionViewModel.Factory
    val navArgs by navArgs<SubmissionSymptomIntroductionFragmentArgs>()
    private val viewModel: SubmissionSymptomIntroductionViewModel by assistedViewModel {
        factory.create(
            testType = navArgs.testType,
            comesFromDispatcherFragment = navArgs.comesFromDispatcherFragment
        )
    }

    private val binding: FragmentSubmissionSymptomIntroBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigation.observe(viewLifecycleOwner) {
            findNavController().navigate(it)
        }

        viewModel.navigateBack.observe(viewLifecycleOwner) {
            popBackStack()
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = submissionCancelDialog { viewModel.onCancelConfirmed() }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        viewModel.showCancelDialog.observe(viewLifecycleOwner) {
            submissionCancelDialog { viewModel.onCancelConfirmed() }
        }

        viewModel.symptomIndication.observe(viewLifecycleOwner) {
            updateButtons(it)
        }

        binding.toolbar.setNavigationOnClickListener { viewModel.onPreviousClicked() }
    }

    private fun updateButtons(symptomIndication: Symptoms.Indication?) {
        binding.targetButtonApply.apply {
            handleColors(symptomIndication, Symptoms.Indication.POSITIVE)
            setOnClickListener { viewModel.onPositiveSymptomIndication() }
        }
        binding.targetButtonReject.apply {
            handleColors(symptomIndication, Symptoms.Indication.NEGATIVE)
            setOnClickListener { viewModel.onNegativeSymptomIndication() }
        }
        binding.targetButtonVerify.apply {
            handleColors(symptomIndication, Symptoms.Indication.NO_INFORMATION)
            setOnClickListener { viewModel.onNoInformationSymptomIndication() }
        }

        binding.symptomButtonNext.apply {
            isActive = symptomIndication != null
            defaultButton.setText(
                when (symptomIndication) {
                    Symptoms.Indication.NEGATIVE -> R.string.submission_done_button_done
                    Symptoms.Indication.NO_INFORMATION -> R.string.submission_done_button_done
                    else -> R.string.submission_symptom_further_button
                }
            )
            setOnClickListener { viewModel.onNextClicked() }
        }
    }

    private fun Button.handleColors(symptomIndication: Symptoms.Indication?, state: Symptoms.Indication) {
        setTextColor(formatSymptomButtonTextStyleByState(context, symptomIndication, state))
        backgroundTintList =
            ColorStateList.valueOf(formatSymptomBackgroundButtonStyleByState(context, symptomIndication, state))
    }

    override fun onResume() {
        super.onResume()
        viewModel.onNewUserActivity()
    }
}
