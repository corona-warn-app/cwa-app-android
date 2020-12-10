package de.rki.coronawarnapp.contactdiary.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ContactDiaryOnboardingFragmentBinding
import de.rki.coronawarnapp.databinding.FragmentOnboardingBinding
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragmentDirections
import de.rki.coronawarnapp.ui.onboarding.OnboardingFragmentViewModel
import de.rki.coronawarnapp.ui.onboarding.OnboardingNavigationEvents
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import kotlinx.android.synthetic.main.contact_diary_onboarding_fragment.*
import javax.inject.Inject

class ContactDiaryOnboardingFragment : Fragment(R.layout.contact_diary_onboarding_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: OnboardingFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryOnboardingFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            contactDiaryOnboardingNextButton.setOnClickListener { }
        }

        vm.routeToScreen.observe2(this) {

        }
    }

    override fun onResume() {
        super.onResume()
        //binding.onboardingContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
