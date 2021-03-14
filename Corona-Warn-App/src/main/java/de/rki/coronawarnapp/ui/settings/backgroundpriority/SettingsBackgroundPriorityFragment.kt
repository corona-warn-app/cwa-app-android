package de.rki.coronawarnapp.ui.settings.backgroundpriority

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsBackgroundPriorityBinding
import de.rki.coronawarnapp.ui.base.startActivitySafely
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

/**
 * This is the setting background priority page. Here the user sees the background priority setting status.
 * If background priority is disabled it can be activated.
 */
class SettingsBackgroundPriorityFragment : Fragment(R.layout.fragment_settings_background_priority), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsBackgroundPriorityFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSettingsBackgroundPriorityBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.backgroundPriorityState.observe2(this) {
            binding.state = it
        }

        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.settingsBackgroundPriorityContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
    }

    private fun setButtonOnClickListener() {
        val settingsRow = binding.settingsRowBackgroundPriority

        // enable background priority
        settingsRow.setOnClickListener {
            (requireActivity() as MainActivity).apply {
                startActivitySafely(powerManagement.toBatteryOptimizationSettingsIntent)
            }
        }

        // explanatory card
        binding.settingsTracingStatusConnection.tracingStatusCardButton.setOnClickListener {
            (requireActivity() as MainActivity).apply {
                startActivity(powerManagement.toBatteryOptimizationSettingsIntent)
            }
        }

        // back navigation
        binding.settingsBackgroundPriorityHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
        }
    }
}
