package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import de.rki.coronawarnapp.util.ui.doNavigate
import javax.inject.Inject

/**
 * Onboarding starting point.
 */
class OnboardingFragment : Fragment(R.layout.fragment_onboarding), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentOnboardingBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            onboardingButtonNext.setOnClickListener { vm.onNextButtonClick() }
            onboardingInclude.onboardingEasyLanguage.setOnClickListener { vm.onEasyLanguageClick() }
        }
        vm.routeToScreen.observe2(this) {
            when (it) {
                is OnboardingNavigationEvents.NavigateToOnboardingPrivacy ->
                    doNavigate(
                        OnboardingFragmentDirections
                            .actionOnboardingFragmentToOnboardingPrivacyFragment()
                    )
                is OnboardingNavigationEvents.NavigateToEasyLanguageUrl ->
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.onboarding_tracing_easy_language_explanation_url))
                        )
                    )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
