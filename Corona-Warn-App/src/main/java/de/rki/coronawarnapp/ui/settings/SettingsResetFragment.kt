package de.rki.coronawarnapp.ui.settings

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsResetBinding
import de.rki.coronawarnapp.ui.dialog.displayDialog
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding

/**
 * The user is informed what a reset means and he can perform it.
 */

@AndroidEntryPoint
class SettingsResetFragment : Fragment(R.layout.fragment_settings_reset) {

    private val vm: SettingsResetViewModel by viewModels()
    private val binding: FragmentSettingsResetBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            settingsResetButtonDelete.setOnClickListener { vm.resetAllData() }
            settingsResetButtonCancel.setOnClickListener { vm.goBack() }
            binding.toolbar.setNavigationOnClickListener { popBackStack() }
        }
        vm.clickEvent.observe(viewLifecycleOwner) {
            when (it) {
                is SettingsEvents.ResetApp -> showConfirmResetDialog()
                is SettingsEvents.GoBack -> popBackStack()
                is SettingsEvents.GoToOnboarding -> navigateToOnboarding()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.settingsResetContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun navigateToOnboarding() {
        OnboardingActivity.start(requireContext())
        activity?.finish()
    }

    private fun showConfirmResetDialog() = displayDialog {
        title(R.string.settings_reset_dialog_headline)
        message(R.string.settings_reset_dialog_body)
        positiveButton(R.string.settings_reset_dialog_button_confirm) { vm.deleteAllAppContent() }
        negativeButton(R.string.settings_reset_dialog_button_cancel)
        setDeleteDialog(true)
    }
}
