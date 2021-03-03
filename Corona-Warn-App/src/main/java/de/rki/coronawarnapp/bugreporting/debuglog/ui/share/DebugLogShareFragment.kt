package de.rki.coronawarnapp.bugreporting.debuglog.ui.share

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.ContactDiarySettings
import de.rki.coronawarnapp.databinding.BugreportingDebuglogShareFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject


class DebugLogShareFragment : Fragment(R.layout.bugreporting_debuglog_share_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory

    @Inject lateinit var settings: ContactDiarySettings

    private val vm: DebugLogShareViewModel by cwaViewModels { viewModelFactory }
    private val binding: BugreportingDebuglogShareFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            debugLogShareButton.setOnClickListener {
                vm.onXXXPress()
            }

            debugLogSharePrivacyInformation.setOnClickListener {
                //vm.onPrivacyButtonPress()
            }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {

                DebugLogShareNavigationEvents.NavigateToXXXFragment -> {
                    /* doNavigate(
                        ContactDiaryOnboardingFragmentDirections
                            .actionContactDiaryOnboardingFragmentToContactDiaryInformationPrivacyFragment()
                    ) */
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
