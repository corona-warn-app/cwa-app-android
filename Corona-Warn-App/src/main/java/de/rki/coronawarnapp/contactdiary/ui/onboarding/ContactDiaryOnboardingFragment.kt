package de.rki.coronawarnapp.contactdiary.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryOnboardingFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.addMenuId
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import java.time.LocalDate
import javax.inject.Inject

class ContactDiaryOnboardingFragment : Fragment(R.layout.contact_diary_onboarding_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val vm: ContactDiaryOnboardingFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryOnboardingFragmentBinding by viewBinding()
    private val args by navArgs<ContactDiaryOnboardingFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false)

        binding.apply {
            contactDiaryOnboardingNextButton.setOnClickListener {
                vm.onNextButtonClick()
            }
            if (!args.showBottomNav) {
                toolbar.apply {
                    addMenuId(R.id.contact_diary_onboarding_fragment_menu_id)
                    navigationIcon = context.getDrawableCompat(R.drawable.ic_close)
                    navigationContentDescription = getString(R.string.accessibility_close)
                    setNavigationOnClickListener { vm.onBackButtonPress() }
                }
            } else {
                binding.root.updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.spacing_fab_padding))
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
                    vm.onboardingComplete()
                    if (arguments?.containsKey(OPEN_CURRENT_DAY) == true) {
                        findNavController().apply {
                            popBackStack(R.id.contactDiaryOnboardingFragment, true)
                            navigate("coronawarnapp://contact-journal/day/${LocalDate.now()}".toUri())
                        }
                    } else {
                        doNavigate(
                            ContactDiaryOnboardingFragmentDirections
                                .actionContactDiaryOnboardingFragmentToContactDiaryOverviewFragment()
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    companion object {
        private const val OPEN_CURRENT_DAY = "goToDay"
    }
}
