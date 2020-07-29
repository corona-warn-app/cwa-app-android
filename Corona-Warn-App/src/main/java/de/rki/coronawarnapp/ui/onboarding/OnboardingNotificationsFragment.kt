package de.rki.coronawarnapp.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentOnboardingNotificationsBinding
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.ConnectivityHelper
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.ExternalActionHelper
import de.rki.coronawarnapp.util.PowerManagementHelper

/**
 * This fragment ask the user if he wants to get notifications and finishes the onboarding afterwards.
 *
 * @see NotificationManagerCompat
 * @see AlertDialog
 */
class OnboardingNotificationsFragment : Fragment() {
    companion object {
        private val TAG: String? = OnboardingNotificationsFragment::class.simpleName
    }

    private var _binding: FragmentOnboardingNotificationsBinding? = null
    private val binding: FragmentOnboardingNotificationsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingNotificationsBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonOnClickListener()
    }

    override fun onResume() {
        super.onResume()
        binding.onboardingNotificationsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }

    private fun setButtonOnClickListener() {
        binding.onboardingButtonNext.setOnClickListener {
            checkForBackgroundJobDisabled()
        }
        binding.onboardingButtonBack.buttonIcon.setOnClickListener {
            (activity as OnboardingActivity).goBack()
        }
    }

    private fun checkForBackgroundJobDisabled() {
        if (!ConnectivityHelper.isBackgroundJobEnabled(requireActivity())) {
            showBackgroundJobDisabledNotification()
        } else {
            checkForEnergyOptimizedEnabled()
        }
    }

    private fun checkForEnergyOptimizedEnabled() {
        if (!PowerManagementHelper.isIgnoringBatteryOptimizations(requireActivity())) {
            showEnergyOptimizedEnabledForBackground()
        } else {
            navigateToMain()
        }
    }

    private fun showBackgroundJobDisabledNotification() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_background_fetch_dialog_headline,
            R.string.onboarding_background_fetch_dialog_body,
            R.string.onboarding_background_fetch_dialog_button_positive,
            R.string.onboarding_background_fetch_dialog_button_negative,
            false,
            {
                val intent = Intent(
                    ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

                // show battery optimization system dialog after background processing dialog
                checkForEnergyOptimizedEnabled()
            },
            {
                // declined, show additional dialog explaining manual risk calculation
                showManualCheckingRequiredDialog()
            })
        DialogHelper.showDialog(dialog)
    }

    private fun showEnergyOptimizedEnabledForBackground() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_energy_optimized_dialog_headline,
            R.string.onboarding_energy_optimized_dialog_body,
            R.string.onboarding_energy_optimized_dialog_button_positive,
            R.string.onboarding_energy_optimized_dialog_button_negative,
            false,
            {
                // go to battery optimization
                ExternalActionHelper.toBatteryOptimizationSettings(requireContext())
                LocalData.energyOptimizedExplanationDialogWasShown(true)
                navigateToMain()
            },
            {
                // keep battery optimization enabled
                LocalData.energyOptimizedExplanationDialogWasShown(true)
                showManualCheckingRequiredDialog()
            })
        DialogHelper.showDialog(dialog)
    }

    private fun showManualCheckingRequiredDialog() {
        val dialog = DialogHelper.DialogInstance(
            requireActivity(),
            R.string.onboarding_manual_required_dialog_headline,
            R.string.onboarding_manual_required_dialog_body,
            R.string.onboarding_manual_required_dialog_button,
            null,
            false,
            {
                navigateToMain()
            }
        )
        DialogHelper.showDialog(dialog)
    }

    private fun navigateToMain() {
        (requireActivity() as OnboardingActivity).completeOnboarding()
    }
}
