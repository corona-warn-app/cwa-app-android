package de.rki.coronawarnapp.srs.ui.typeselection

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSrsTypeSelectionBinding
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.formatter.formatSrsTypeSelectionBackgroundButtonStyleByState
import de.rki.coronawarnapp.util.formatter.formatSrsTypeSelectionButtonTextStyleByState
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SrsTypeSelectionFragment : Fragment(R.layout.fragment_srs_type_selection), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SrsTypeSelectionFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding by viewBinding<FragmentSrsTypeSelectionBinding>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.navigation.observe2(this) {
            when (it) {
                SrsTypeSelectionNavigationEvents.NavigateToCloseDialog -> showCancelDialog()
                SrsTypeSelectionNavigationEvents.NavigateToMainScreen ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections.actionSrsSubmissionTypeSelectionFragmentToMainFragment()
                    )
                SrsTypeSelectionNavigationEvents.NavigateToShareCheckins ->
                    findNavController().navigate(
                        SrsTypeSelectionFragmentDirections
                            .actionSrsSubmissionTypeSelectionFragmentToSrsCheckinsFragment(
                                viewModel.srsTestType.value ?: SrsSubmissionType.SRS_OTHER
                            )
                    )
            }
        }

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onCancel()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.toolbar.setNavigationOnClickListener { viewModel.onCancel() }

        viewModel.srsTestType.observe2(this) {
            updateButtons(it)
        }
    }

    private fun updateButtons(submissionType: SrsSubmissionType?) {

        binding.targetButtonRatRegisteredNoResult.apply {
            handleColors(submissionType, SrsSubmissionType.SRS_RAT)
            setOnClickListener { viewModel.onRatRegisteredNoResult() }
        }

        binding.targetButtonRatNotRegistered.apply {
            // TODO: RAT unregistered
            handleColors(submissionType, SrsSubmissionType.SRS_RAT)
            setOnClickListener { viewModel.onRatNotRegistered() }
        }

        binding.targetButtonPcrRegisteredNoResult.apply {
            handleColors(submissionType, SrsSubmissionType.SRS_REGISTERED_PCR)
            setOnClickListener { viewModel.onPcrRegisteredNoResult() }
        }

        binding.targetButtonPcrNotRegistered.apply {
            handleColors(submissionType, SrsSubmissionType.SRS_UNREGISTERED_PCR)
            setOnClickListener { viewModel.onPcrNotRegistered() }
        }

        binding.targetButtonRapidPcr.apply {
            handleColors(submissionType, SrsSubmissionType.SRS_RAPID_PCR)
            setOnClickListener { viewModel.onRapidPcr() }
        }

        binding.targetButtonOther.apply {
            handleColors(submissionType, SrsSubmissionType.SRS_OTHER)
            setOnClickListener { viewModel.onOther() }
        }

        binding.typeSelectionNextButton.apply {
            isEnabled = submissionType != null
            setOnClickListener { viewModel.onNextClicked() }
        }
    }

    private fun Button.handleColors(submissionType: SrsSubmissionType?, state: SrsSubmissionType) {
        setTextColor(formatSrsTypeSelectionButtonTextStyleByState(context, state, submissionType))
        backgroundTintList =
            ColorStateList.valueOf(formatSrsTypeSelectionBackgroundButtonStyleByState(context, state, submissionType))
    }

    private fun showCancelDialog() {
        displayDialog {
            title(R.string.srs_cancel_dialog_title)
            message(R.string.srs_cancel_dialog_body)
            positiveButton(R.string.srs_cancel_dialog_positive_button)
            negativeButton(R.string.srs_cancel_dialog_negative_button) { viewModel.onCancelConfirmed() }
        }
    }

    companion object {
        fun uri() = "cwa://srs-consent/openTypeSelection".toUri()
    }
}
