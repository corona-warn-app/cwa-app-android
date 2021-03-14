package de.rki.coronawarnapp.ui.submission.qrcode.consent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSubmissionConsentBinding
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class SubmissionConsentFragment : Fragment(R.layout.fragment_submission_consent), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: SubmissionConsentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSubmissionConsentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.submissionConsentHeader.headerButtonBack.buttonIcon.setOnClickListener {
            viewModel.onBackButtonClick()
        }
        viewModel.routeToScreen.observe2(this) {
            when (it) {
                is SubmissionNavigationEvents.NavigateToQRCodeScan -> doNavigate(
                    SubmissionConsentFragmentDirections.actionSubmissionConsentFragmentToSubmissionQRCodeScanFragment()
                )
                is SubmissionNavigationEvents.NavigateToDispatcher -> popBackStack()
                is SubmissionNavigationEvents.NavigateToDataPrivacy -> doNavigate(
                    SubmissionConsentFragmentDirections.actionSubmissionConsentFragmentToInformationPrivacyFragment()
                )
                is SubmissionNavigationEvents.ResolvePlayServicesException ->
                    it.exception.status.startResolutionForResult(
                        requireActivity(),
                        REQUEST_USER_RESOLUTION
                    )
            }
        }
        viewModel.countries.observe2(this) {
            binding.countries = it
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_USER_RESOLUTION) {
            viewModel.giveGoogleConsentResult(resultCode == Activity.RESULT_OK)
        }
    }

    companion object {
        private const val REQUEST_USER_RESOLUTION = 3000
    }
}
