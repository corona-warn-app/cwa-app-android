package de.rki.coronawarnapp.ui.submission.symptoms.introduction

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionSymptomIntroBinding
import de.rki.coronawarnapp.submission.Symptoms
import de.rki.coronawarnapp.ui.submission.SubmissionBlockingDialog
import de.rki.coronawarnapp.ui.submission.SubmissionCancelDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatButtonStyleByState
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * The [SubmissionSymptomIntroductionFragment], initial fragment displayed when the user starts the submission process
 * providing symptoms, asking whether or not the user has experienced any of the common symptoms of COVID-19.
 */
class SubmissionSymptomIntroductionFragment : Fragment(R.layout.fragment_submission_symptom_intro),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionSymptomIntroductionViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSubmissionSymptomIntroBinding by viewBindingLazy()
    private lateinit var uploadDialog: SubmissionBlockingDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uploadDialog = SubmissionBlockingDialog(requireContext())

        viewModel.navigation.observe2(this) {
            doNavigate(it)
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
