package de.rki.coronawarnapp.contactdiary.ui.overview

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.ActionOnlyNavDirections
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.ContactDiaryActivity
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingFragmentViewModel
import de.rki.coronawarnapp.contactdiary.ui.onboarding.ContactDiaryOnboardingNavigationEvents
import de.rki.coronawarnapp.databinding.ContactDiaryOnboardingFragmentBinding
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class ContactDiaryOverviewFragment : Fragment(R.layout.contact_diary_overview_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: ContactDiaryOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding: ContactDiaryOverviewFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

        }

        vm.routeToScreen.observe2(this) {

        }
    }

    override fun onResume() {
        super.onResume()
    }
}
