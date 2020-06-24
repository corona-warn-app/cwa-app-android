package de.rki.coronawarnapp.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import de.rki.coronawarnapp.util.DataRetentionHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The user is informed what a reset means and he can perform it.
 *
 */
class SettingsResetFragment : Fragment() {

    companion object {
        private val TAG: String? = SettingsResetFragment::class.simpleName
    }

    private var _binding: FragmentSettingsResetBinding? = null
    private val binding: FragmentSettingsResetBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsResetBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsResetButtonDelete.setOnClickListener {
            confirmReset()
        }
        binding.settingsResetButtonCancel.setOnClickListener {
            (activity as MainActivity).goBack()
        }
        binding.settingsResetHeader.headerButtonBack.buttonIcon.setOnClickListener {
            (activity as MainActivity).goBack()
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

    private fun deleteLocalAppContent() {
        DataRetentionHelper.clearAllLocalData(requireContext())
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
