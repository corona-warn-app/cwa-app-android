package de.rki.coronawarnapp.ui.submission.yourconsent

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionYourConsentBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * [SubmissionYourConsentFragment] allows the user to provide or revoke consent via a switch on the screen. This screen
 * is accessed via the TestResultAvailableFragment flow.
 */
class SubmissionYourConsentFragment : Fragment(R.layout.fragment_submission_your_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SubmissionYourConsentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionYourConsentBinding by viewBindingLazy()
    private val navArgs: SubmissionYourConsentFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.consent.observe2(this) {
            binding.submissionYourConsentSwitch.status = it
            binding.submissionYourConsentSwitch.statusText = getString(
                if (it) {
                    R.string.submission_your_consent_switch_status_on
                } else {
                    R.string.submission_your_consent_switch_status_off
                }
            )
        }

        vm.countryList.observe2(this) {
            binding.submissionYourConsentAgreementCountryList.countries = it
        }

        vm.clickEvent.observe2(this) {
            when (it) {
                is SubmissionYourConsentEvents.GoBack -> (activity as MainActivity).goBack()
                is SubmissionYourConsentEvents.GoLegal -> doNavigate(
                    SubmissionYourConsentFragmentDirections
                        .actionSubmissionYourConsentFragmentToInformationPrivacyFragment()
                )
            }
        }

        binding.apply {
            submissionYourConsentTitle.headerButtonBack.buttonIcon.setOnClickListener { vm.goBack() }
            submissionYourConsentSwitch.settingsSwitchRowSwitch.setOnCheckedChangeListener { view, _ ->
                if (!view.isPressed) return@setOnCheckedChangeListener
                vm.switchConsent()
            }
            submissionYourConsentSwitch.settingsSwitchRow.setOnClickListener { vm.switchConsent() }
            submissionYourConsentAgreementDetailsText.setOnClickListener { vm.goLegal() }

            submissionYourConsentAgreementShareSymptomsText.setText(
                if (navArgs.isTestResultAvailable) {
                    R.string.submission_your_consent_agreement_share_symptoms_2
                } else {
                    R.string.submission_your_consent_agreement_share_symptoms
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionYourConsentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
