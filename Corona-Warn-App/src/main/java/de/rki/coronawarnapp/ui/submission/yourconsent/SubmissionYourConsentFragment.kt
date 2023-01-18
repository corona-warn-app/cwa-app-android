package de.rki.coronawarnapp.ui.submission.yourconsent

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionYourConsentBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModelsAssisted
import javax.inject.Inject

/**
 * [SubmissionYourConsentFragment] allows the user to provide or revoke consent via a switch on the screen. This screen
 * is accessed via the TestResultAvailableFragment flow.
 */
class SubmissionYourConsentFragment : Fragment(R.layout.fragment_submission_your_consent), AutoInject {

    private val navArgs by navArgs<SubmissionYourConsentFragmentArgs>()
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SubmissionYourConsentViewModel by cwaViewModelsAssisted(
        factoryProducer = { viewModelFactory },
        constructorCall = { factory, _ ->
            factory as SubmissionYourConsentViewModel.Factory
            factory.create(navArgs.testType)
        }
    )
    private val binding: FragmentSubmissionYourConsentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.consent.observe2(this) {
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

        vm.countryList.observe2(this) {
            binding.submissionYourConsentAgreementCountryList.countries = it
        }

        vm.clickEvent.observe2(this) {
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

        vm.errorEvent.observe2(this) { displayDialog { setError(it) } }
    }

    override fun onResume() {
        super.onResume()
        binding.submissionYourConsentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
