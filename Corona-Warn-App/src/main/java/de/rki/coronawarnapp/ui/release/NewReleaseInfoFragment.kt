package de.rki.coronawarnapp.ui.release

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.NewReleaseInfoScreenFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class NewReleaseInfoFragment : Fragment(R.layout.new_release_info_screen_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    private val vm: NewReleaseInfoFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: NewReleaseInfoScreenFragmentBinding by viewBindingLazy()
    private val args: NewReleaseInfoFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            newReleaseInfoNextButton.setOnClickListener {
                vm.userHasReadReleaseNotes()
                vm.onNextButtonClick()
            }

            newReleaseInfoToolbar.setNavigationOnClickListener {
                vm.userHasReadReleaseNotes()
                vm.onNextButtonClick()
            }
        }

        vm.appVersion.observe2(this) {
            binding.newReleaseInfoHeadline.text = it
        }

        if(args.comesFromInfoScreen){
            vm.navigationIcon.observe2(this) {
                binding.newReleaseInfoToolbar.navigationIcon = it
            }
        }

        binding.newReleaseInfoNextButton.isGone = args.comesFromInfoScreen

        vm.routeToScreen.observe2(this) {
            if (it is NewReleaseInfoFragmentNavigationEvents.NavigateToMainActivity) {
                doNavigate(NewReleaseInfoFragmentDirections.actionNewReleaseInfoFragmentPop())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.newReleaseInfoScreenContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
