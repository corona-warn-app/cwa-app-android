package de.rki.coronawarnapp.contactdiary.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.databinding.ContactDiaryOnboardingFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryOnboardingFragment : Fragment(R.layout.contact_diary_onboarding_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    @Inject lateinit var settings: ContactDiarySettings

    private val vm: ContactDiaryOnboardingFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryOnboardingFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            contactDiaryOnboardingNextButton.setOnClickListener {

                vm.onNextButtonClick()
            }

            toolbar.setNavigationOnClickListener {
                vm.onBackButtonPress()
            }

            contactDiaryOnboardingPrivacyInformation.setOnClickListener {
                vm.onPrivacyButtonPress()
            }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {

                ContactDiaryOnboardingNavigationEvents.NavigateToMainActivity -> {
                    requireActivity().onBackPressed()
                }

                ContactDiaryOnboardingNavigationEvents.NavigateToPrivacyFragment -> {
                    doNavigate(
                        ContactDiaryOnboardingFragmentDirections
                            .actionContactDiaryOnboardingFragmentToContactDiaryInformationPrivacyFragment()
                    )
                }

                ContactDiaryOnboardingNavigationEvents.NavigateToOverviewFragment -> {
                    onboardingComplete()
                    doNavigate(
                        ContactDiaryOnboardingFragmentDirections
                            .actionContactDiaryOnboardingFragmentToContactDiaryOverviewFragment()
                    )
                }
            }
        }
    }

    fun onboardingComplete() {
        settings.isOnboarded.update { true }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
