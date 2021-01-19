package de.rki.coronawarnapp.ui.release

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.NewReleaseInfoScreenFragmentBinding
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class NewReleaseInfoFragment : Fragment(R.layout.new_release_info_screen_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    @Inject lateinit var settings: CWASettings

    private val vm: NewReleaseInfoFragmentViewModel by cwaViewModels { viewModelFactory }
    private val binding: NewReleaseInfoScreenFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            newReleaseInfoNextButton.setOnClickListener {
                userHasReadReleaseNotes()
                vm.onNextButtonClick()
            }

            newReleaseInfoToolbar.setNavigationOnClickListener {
                userHasReadReleaseNotes()
                vm.onNextButtonClick()
            }
        }

        vm.appVersion.observe2(this) {
            binding.newReleaseInfoHeadline.text = it
        }

        vm.routeToScreen.observe2(this) {
            if (it is NewReleaseInfoFragmentNavigationEvents.NavigateToMainActivity) {
                doNavigate(NewReleaseInfoFragmentDirections.actionNewReleaseInfoFragmentPop())
            }
        }
    }

    fun userHasReadReleaseNotes() {
        settings.lastAppVersion.update { BuildConfigWrap.VERSION_CODE }
    }

    override fun onResume() {
        super.onResume()
        binding.newReleaseInfoScreenContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
