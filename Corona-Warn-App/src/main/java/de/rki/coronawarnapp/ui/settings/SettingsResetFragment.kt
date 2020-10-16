package de.rki.coronawarnapp.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsResetBinding
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The user is informed what a reset means and he can perform it.
 *
 */
class SettingsResetFragment : Fragment(R.layout.fragment_settings_reset), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: SettingsResetViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSettingsResetBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            settingsResetButtonDelete.setOnClickListener { vm.resetAllData() }
            settingsResetButtonCancel.setOnClickListener { vm.goBack() }
            settingsResetHeader.headerButtonBack.buttonIcon.setOnClickListener { vm.goBack() }
        }
        vm.clickEvent.observe2(this) {
            when (it) {
                is SettingsEvents.ResetApp -> confirmReset()
                is SettingsEvents.GoBack -> (activity as MainActivity).goBack()
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

    private fun confirmReset() {
        val resetDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.settings_reset_dialog_headline,
            R.string.settings_reset_dialog_body,
            R.string.settings_reset_dialog_button_confirm,
            R.string.settings_reset_dialog_button_cancel,
            cancelable = true,
            positiveButtonFunction = {
                lifecycleScope.launch { vm.deleteAllAppContent() }
                Unit
            }
        )

        DialogHelper.showDialog(resetDialog).apply {
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorTextSemanticRed))
        }
    }
}
