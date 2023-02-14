package de.rki.coronawarnapp.contactdiary.ui.onboarding

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.net.toUri
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryOnboardingFragmentBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.ui.addMenuId
import de.rki.coronawarnapp.util.ui.viewBinding
import java.time.LocalDate

@AndroidEntryPoint
class ContactDiaryOnboardingFragment : Fragment(R.layout.contact_diary_onboarding_fragment) {

    private val vm: ContactDiaryOnboardingFragmentViewModel by viewModels()
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
                binding.root.updatePadding(bottom = resources.getDimensionPixelSize(R.dimen.padding_80))
            }

            contactDiaryOnboardingPrivacyInformation.setOnClickListener {
                vm.onPrivacyButtonPress()
            }
        }

        vm.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {

                ContactDiaryOnboardingNavigationEvents.NavigateToMainActivity -> {
                    requireActivity().onBackPressed()
                }

                ContactDiaryOnboardingNavigationEvents.NavigateToPrivacyFragment -> {
                    findNavController().navigate(
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
                        findNavController().navigate(
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
