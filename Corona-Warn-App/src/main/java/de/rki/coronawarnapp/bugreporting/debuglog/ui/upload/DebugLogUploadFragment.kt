package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.BugreportingDebuglogUploadFragmentBinding
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class DebugLogUploadFragment : Fragment(R.layout.bugreporting_debuglog_upload_fragment), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: DebugLogUploadViewModel by cwaViewModels { viewModelFactory }
    private val binding: BugreportingDebuglogUploadFragmentBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            debugLogShareButton.setOnClickListener {
                vm.onUploadLog()
            }

            debugLogSharePrivacyInformation.setOnClickListener {
                vm.onPrivacyButtonPress()
            }

            toolbar.setNavigationOnClickListener { popBackStack() }
        }

        vm.routeToScreen.observe2(this) {
            when (it) {

                DebugLogUploadNavigationEvents.NavigateToMoreInformationFragment -> {
                    doNavigate(
                        DebugLogUploadFragmentDirections.actionDebugLogUploadFragmentToDebugLogLegalFragment()
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.contentContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
