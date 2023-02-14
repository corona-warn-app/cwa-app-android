package de.rki.coronawarnapp.datadonation.survey.consent

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.SurveyConsentFragmentBinding
import de.rki.coronawarnapp.ui.dialog.CwaDialogFragment
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ExternalActionHelper.openUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SurveyConsentFragment : Fragment(R.layout.survey_consent_fragment) {

    @Inject lateinit var factory: SurveyConsentViewModel.Factory
    private val navArgs by navArgs<SurveyConsentFragmentArgs>()
    private val vm: SurveyConsentViewModel by assistedViewModel {
        factory.create(navArgs.SurveyType)
    }

    private val binding: SurveyConsentFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            toolbar.setNavigationOnClickListener { vm.onBackButtonPressed() }
            surveyNextButton.setOnClickListener { vm.onNextButtonPressed() }
            surveyConsentMoreInfo.setOnClickListener { vm.onMoreInformationButtonPressed() }
        }

        vm.routeToScreen.observe(viewLifecycleOwner) { event ->
            when (event) {
                is SurveyConsentNavigationEvents.NavigateBack ->
                    activity?.onBackPressed()

                is SurveyConsentNavigationEvents.NavigateWeb -> {
                    openUrl(event.url)
                    popBackStack()
                }

                is SurveyConsentNavigationEvents.NavigateToMoreInformationScreen -> {
                    findNavController().navigate(
                        SurveyConsentFragmentDirections.actionSurveyConsentFragmentToSurveyConsentDetailFragment()
                    )
                }
            }
        }

        vm.showLoadingIndicator.observe(viewLifecycleOwner) { loading ->
            if (loading) showSurveyProgressDialog()
            else {
                val tag = SurveyConsentFragment::class.java.simpleName + CwaDialogFragment.TAG
                childFragmentManager.findFragmentByTag(tag)
                    ?.let { (it as CwaDialogFragment).dismiss() }
            }
        }

        vm.showErrorDialog.observe(viewLifecycleOwner) {
            showErrorDialog(it.errorMessage.get(requireContext()))
        }
    }

    private fun showSurveyProgressDialog() = displayDialog {
        setCancelable(false)
        setView(R.layout.survey_consent_blocking_progress_dialog)
    }

    private fun showErrorDialog(message: String) = displayDialog {
        title(R.string.datadonation_details_survey_consent_error_dialog_title)
        message(message)
        positiveButton(R.string.datadonation_details_survey_consent_error_dialog_pos_button)
        setCancelable(false)
    }
}
