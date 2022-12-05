package de.rki.coronawarnapp.srs.ui.symptoms.intro

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.srs.ui.dialogs.showCloseDialog
import de.rki.coronawarnapp.srs.ui.dialogs.showSubmissionWarningDialog
import de.rki.coronawarnapp.srs.ui.dialogs.showTruncatedSubmissionDialog
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatSymptomBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatSymptomButtonTextStyleByState
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

class SrsSymptomsIntroductionFragment : Fragment(R.layout.fragment_submission_symptom_intro), AutoInject {

    private val navArgs by navArgs<SrsSymptomsIntroductionFragmentArgs>()

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SrsSymptomsIntroductionViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SrsSymptomsIntroductionViewModel.Factory
            factory.create(
                submissionType = navArgs.submissionType,
                selectedCheckIns = navArgs.selectedCheckIns
            )
        }
    )

    private val binding: FragmentSubmissionSymptomIntroBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onCancelConfirmed()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.toolbar.setNavigationOnClickListener { viewModel.onCancelConfirmed() }

        viewModel.symptomIndication.observe2(this) {
            updateButtons(it)
        }

        viewModel.showLoadingIndicator.observe(viewLifecycleOwner) { binding.symptomButtonNext.isLoading = it }

        viewModel.events.observe(viewLifecycleOwner) {
            when (it) {
                SrsSymptomsIntroductionNavigation.ShowCloseDialog -> showCloseDialog { viewModel.goHome() }
                SrsSymptomsIntroductionNavigation.ShowSubmissionWarning -> {
                    showSubmissionWarningDialog { viewModel.onWarningClicked() }
                }

                SrsSymptomsIntroductionNavigation.GoToHome -> findNavController().navigate(
                    SrsSymptomsIntroductionFragmentDirections.actionSrsSymptomsIntroductionFragmentToMainFragment()
                )

                is SrsSymptomsIntroductionNavigation.GoToThankYouScreen -> findNavController().navigate(
                    SrsSymptomsIntroductionFragmentDirections
                        .actionSrsSymptomsIntroductionFragmentToSrsSubmissionDoneFragment(it.submissionType)
                )

                is SrsSymptomsIntroductionNavigation.GoToSymptomCalendar -> findNavController().navigate(
                    SrsSymptomsIntroductionFragmentDirections
                        .actionSrsSymptomsIntroductionFragmentToSrsSymptomsCalendarFragment(
                            submissionType = it.submissionType,
                            symptomIndication = it.symptomIndication,
                            selectedCheckIns = it.selectedCheckins
                        )
                )

                is SrsSymptomsIntroductionNavigation.TruncatedSubmission -> {
                    showTruncatedSubmissionDialog(it.numberOfDays) {
                        viewModel.onTruncatedDialogClick()
                    }
                }

                is SrsSymptomsIntroductionNavigation.Error -> displayDialog {
                    setError(it.cause)
                    positiveButton(R.string.nm_faq_label) { openUrl(R.string.srs_faq_url) }
                    negativeButton(android.R.string.ok)
                }
            }
        }
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
            setOnClickListener { viewModel.onNextClick() }
        }
    }

    private fun Button.handleColors(symptomIndication: Symptoms.Indication?, state: Symptoms.Indication) {
        setTextColor(formatSymptomButtonTextStyleByState(context, symptomIndication, state))
        backgroundTintList =
            ColorStateList.valueOf(formatSymptomBackgroundButtonStyleByState(context, symptomIndication, state))
    }
}
