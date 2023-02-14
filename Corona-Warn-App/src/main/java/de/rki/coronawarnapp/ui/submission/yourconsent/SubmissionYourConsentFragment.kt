package de.rki.coronawarnapp.ui.submission.yourconsent

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionYourConsentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.assistedViewModel
import javax.inject.Inject

/**
 * [SubmissionYourConsentFragment] allows the user to provide or revoke consent via a switch on the screen. This screen
 * is accessed via the TestResultAvailableFragment flow.
 */
@AndroidEntryPoint
class SubmissionYourConsentFragment : Fragment(R.layout.fragment_submission_your_consent) {

    @Inject lateinit var factory: SubmissionYourConsentViewModel.Factory
    private val navArgs by navArgs<SubmissionYourConsentFragmentArgs>()
    private val vm: SubmissionYourConsentViewModel by assistedViewModel {
        factory.create(navArgs.testType)
    }
    private val binding: FragmentSubmissionYourConsentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submissionYourConsentSwitch.setSwitchEnabled(true)

        vm.consent.observe(viewLifecycleOwner) {
            binding.submissionYourConsentSwitch.setChecked(it)
            binding.submissionYourConsentSwitch.setSubtitle(
                getString(
                    if (it) {
                        R.string.submission_your_consent_switch_status_on
                    } else {
                        R.string.submission_your_consent_switch_status_off
                    }
                )
            )
        }

        vm.countryList.observe(viewLifecycleOwner) {
            binding.submissionYourConsentAgreementCountryList.countries = it
        }

        vm.clickEvent.observe(viewLifecycleOwner) {
            when (it) {
                is SubmissionYourConsentEvents.GoBack -> popBackStack()
                is SubmissionYourConsentEvents.GoLegal -> findNavController().navigate(
                    SubmissionYourConsentFragmentDirections
                        .actionSubmissionYourConsentFragmentToInformationPrivacyFragment()
                )
            }
        }

        binding.apply {
            binding.toolbar.setNavigationOnClickListener { vm.goBack() }
            submissionYourConsentSwitch.setOnClickListener { vm.switchConsent() }
            submissionConsentMoreInfo.setOnClickListener { vm.goLegal() }

            submissionYourConsentAgreementShareSymptomsText.setText(
                if (navArgs.isTestResultAvailable) {
                    R.string.submission_your_consent_agreement_share_symptoms_2
                } else {
                    R.string.submission_your_consent_agreement_share_symptoms
                }
            )
        }

        vm.errorEvent.observe(viewLifecycleOwner) { displayDialog { setError(it) } }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionYourConsentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
