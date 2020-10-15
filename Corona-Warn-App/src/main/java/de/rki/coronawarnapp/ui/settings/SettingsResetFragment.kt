package de.rki.coronawarnapp.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.api.ApiException
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsResetBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.ui.main.MainActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingActivity
import de.rki.coronawarnapp.ui.onboarding.OnboardingTracingFragmentViewModel
import de.rki.coronawarnapp.util.DataReset
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The user is informed what a reset means and he can perform it.
 *
 */
class SettingsResetFragment() : Fragment(R.layout.fragment_settings_reset), AutoInject {

    companion object {
        private val TAG: String? = SettingsResetFragment::class.simpleName
    }

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    @Inject lateinit var dataReset: DataReset
    private val vm: SettingsResetViewModel by cwaViewModels { viewModelFactory }
    private val binding: FragmentSettingsResetBinding by viewBindingLazy()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            settingsResetButtonDelete.setOnClickListener{ vm.resetAllData() }
            settingsResetButtonCancel.setOnClickListener { vm.goBack() }
            settingsResetHeader.headerButtonBack.buttonIcon.setOnClickListener { vm.goBack() }
        }
        vm.clickEvent.observe2(this) {
            when (it) {
                is SettingsEvents.ResetApp -> confirmReset()
                is SettingsEvents.GoBack -> (activity as MainActivity).goBack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.settingsResetContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun deleteAllAppContent() {
        lifecycleScope.launch {
            try {
                val isTracingEnabled = InternalExposureNotificationClient.asyncIsEnabled()
                // only stop tracing if it is currently enabled
                if (isTracingEnabled) {
                    InternalExposureNotificationClient.asyncStop()
                    BackgroundWorkScheduler.stopWorkScheduler()
                }
            } catch (apiException: ApiException) {
                apiException.report(
                    ExceptionCategory.EXPOSURENOTIFICATION, TAG, null
                )
            }
            withContext(Dispatchers.IO) {
                deleteLocalAppContent()
            }
            navigateToOnboarding()
        }
    }

    private fun navigateToOnboarding() {
        OnboardingActivity.start(requireContext())
        activity?.finish()
    }

    private suspend fun deleteLocalAppContent() {
        dataReset.clearAllLocalData()
    }

    private fun confirmReset() {
        val resetDialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.settings_reset_dialog_headline,
            R.string.settings_reset_dialog_body,
            R.string.settings_reset_dialog_button_confirm,
            R.string.settings_reset_dialog_button_cancel,
            true,
            {
                deleteAllAppContent()
            }
        )

        DialogHelper.showDialog(resetDialog).apply {
            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getColor(R.color.colorTextSemanticRed))
        }
    }
}
