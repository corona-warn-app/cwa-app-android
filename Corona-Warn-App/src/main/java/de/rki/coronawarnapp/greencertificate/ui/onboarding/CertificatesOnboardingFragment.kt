package de.rki.coronawarnapp.greencertificate.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentCertificatesOnboardingBinding
import de.rki.coronawarnapp.greencertificate.ui.CertificatesSettings
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class CertificatesOnboardingFragment : Fragment(R.layout.fragment_certificates_onboarding), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    @Inject lateinit var settings: CertificatesSettings

    private val viewModel by cwaViewModels<CertificatesOnboardingViewModel> { viewModelFactory }
    private val binding by viewBinding<FragmentCertificatesOnboardingBinding>()
    private val args by navArgs<CertificatesOnboardingFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        binding.apply {
            onboardingNextButton.setOnClickListener {
                viewModel.onNextButtonClick()
            }
            if (!args.showBottomNav) {
                toolbar.apply {
                    navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                    navigationContentDescription = getString(R.string.accessibility_close)
                    setNavigationOnClickListener { viewModel.onBackButtonPress() }
                }
            }
            onboardingPrivacy.setOnClickListener {
                viewModel.onPrivacyButtonPress()
            }
        }

        viewModel.routeToScreen.observe2(this) {
            when (it) {

                CertificatesOnboardingNavigationEvents.NavigateToMainActivity -> {
                    requireActivity().onBackPressed()
                }

                CertificatesOnboardingNavigationEvents.NavigateToPrivacyFragment -> {
                    doNavigate(
                        CertificatesOnboardingFragmentDirections
                            .actionCertificatesOnboardingFragmentToCheckInPrivacyFragment()
                    )
                }

                CertificatesOnboardingNavigationEvents.NavigateToOverviewFragment -> {
                    onboardingComplete()
                    doNavigate(
                        CertificatesOnboardingFragmentDirections
                            .actionCertificatesOnboardingFragmentToCertificatesFragment()
                    )
                }
            }
        }
    }

    private fun onboardingComplete() {
        settings.onboardingStatus = CertificatesSettings.OnboardingStatus.ONBOARDED
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
