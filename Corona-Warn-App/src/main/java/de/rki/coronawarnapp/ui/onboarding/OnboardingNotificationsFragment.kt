package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingNotificationsBinding
import de.rki.coronawarnapp.storage.SettingsRepository
import de.rki.coronawarnapp.ui.BaseFragment
import de.rki.coronawarnapp.util.DialogHelper

/**
 * This fragment ask the user if he wants to get notifications and finishes the onboarding afterwards.
 *
 * @see NotificationManagerCompat
 * @see AlertDialog
 */
class OnboardingNotificationsFragment : BaseFragment() {
    companion object {
        private val TAG: String? = OnboardingNotificationsFragment::class.simpleName
    }

    private lateinit var binding: FragmentOnboardingNotificationsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnboardingNotificationsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonFinish.setOnClickListener {
            finishOrSettings()
        }
        binding.onboardingButtonDisable.setOnClickListener {
            // Set default value for notifications to false
            SettingsRepository.toggleNotificationsRiskEnabled()
            SettingsRepository.toggleNotificationsTestEnabled()
            navigateToMain()
        }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }
    }

    private fun finishOrSettings() {
        // If the os notifications settings are enabled the user can finish the onboarding.
        // If the user want to get notifications but they are disabled on the os level he can go
        // to the notification settings to activate them.
        val areNotificationsEnabled =
            NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
        if (areNotificationsEnabled) {
            navigateToMain()
        } else {
            showNotificationsDisabledDialog()
        }
    }

    private fun navigateToMain() {
        (requireActivity() as OnboardingActivity).completeOnboarding()
    }

    private fun showNotificationsDisabledDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_notifications_headline,
            R.string.onboarding_notifications_dialog_body,
            R.string.onboarding_notifications_dialog_button_positive,
            R.string.onboarding_notifications_dialog_button_negative,
            {
                navigateToNotificationSettings()
            }, {
                navigateToMain()
            })
        DialogHelper.showDialog(dialog)
    }

    private fun navigateToNotificationSettings() {
        val intent = Intent()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(TAG, "current")
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(
                Settings.EXTRA_APP_PACKAGE,
                requireContext().packageName
            )
        } else {
            Log.i(TAG, "old")
            intent.putExtra(
                "app_package",
                requireContext().packageName
            )
            intent.putExtra("app_uid", requireContext().applicationInfo.uid)
        }
        startActivity(intent)
    }
}
