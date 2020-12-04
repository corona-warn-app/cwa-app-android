package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.ui.submission.SubmissionCancelDialog
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatButtonStyleByState
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionSymptomIntroductionFragment : Fragment(R.layout.fragment_submission_symptom_intro),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionSymptomIntroductionViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionSymptomIntroBinding by viewBindingLazy()
    private lateinit var uploadDialog: SubmissionBlockingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadDialog = SubmissionBlockingDialog(requireContext())

        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToSymptomCalendar -> doNavigate(
                    SubmissionSymptomIntroductionFragmentDirections
                        .actionSubmissionSymptomIntroductionFragmentToSubmissionSymptomCalendarFragment()
                )
                is SubmissionNavigationEvents.NavigateToMainActivity -> doNavigate(
                    SubmissionSymptomIntroductionFragmentDirections
                        .actionSubmissionSymptomIntroductionFragmentToMainFragment()
                )
            }
        }

        viewModel.showCancelDialog.observe2(this) {
            SubmissionCancelDialog(requireContext()).show {
                viewModel.onCancelConfirmed()
            }
        }

        viewModel.showUploadDialog.observe2(this) {
            uploadDialog.setState(show = it)
        }

        viewModel.symptomIndication.observe2(this) {
            updateButtons(it)
        }

        val backCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onPreviousClicked()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.submissionSymptomHeader.headerButtonBack.buttonIcon.setOnClickListener { viewModel.onPreviousClicked() }
    }

    private fun updateButtons(symptomIndication: Symptoms.Indication?) {
        binding.targetButtonApply.apply {
            setTextColor(formatButtonStyleByState(symptomIndication, Symptoms.Indication.POSITIVE))
            backgroundTintList = ColorStateList.valueOf(
                formatBackgroundButtonStyleByState(symptomIndication, Symptoms.Indication.POSITIVE)
            )
            setOnClickListener { viewModel.onPositiveSymptomIndication() }
        }
        binding.targetButtonReject.apply {
            setTextColor(formatButtonStyleByState(symptomIndication, Symptoms.Indication.NEGATIVE))
            backgroundTintList = ColorStateList.valueOf(
                formatBackgroundButtonStyleByState(symptomIndication, Symptoms.Indication.NEGATIVE)
            )
            setOnClickListener { viewModel.onNegativeSymptomIndication() }
        }
        binding.targetButtonVerify.apply {
            setTextColor(formatButtonStyleByState(symptomIndication, Symptoms.Indication.NO_INFORMATION))
            backgroundTintList =
                ColorStateList.valueOf(
                    formatBackgroundButtonStyleByState(symptomIndication, Symptoms.Indication.NO_INFORMATION)
                )

            setOnClickListener { viewModel.onNoInformationSymptomIndication() }
        }

        binding.symptomButtonNext.apply {
            isEnabled = symptomIndication != null
            setText(
                when (symptomIndication) {
                    Symptoms.Indication.NEGATIVE -> R.string.submission_done_button_done
                    Symptoms.Indication.NO_INFORMATION -> R.string.submission_done_button_done
                    else -> R.string.submission_symptom_further_button
                }
            )
            setOnClickListener { viewModel.onNextClicked() }
        }
    }
}
