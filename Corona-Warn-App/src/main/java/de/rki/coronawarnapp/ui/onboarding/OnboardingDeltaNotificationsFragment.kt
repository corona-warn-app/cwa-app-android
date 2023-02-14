package de.rki.coronawarnapp.ui.onboarding

import TextViewUrlSet
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.util.ui.viewBinding
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.rki.coronawarnapp.databinding.NotificationsDeltaOnboardingFragmentBinding
import de.rki.coronawarnapp.util.ui.toResolvingString
import setTextWithUrls

@AndroidEntryPoint
class OnboardingDeltaNotificationsFragment : Fragment(R.layout.notifications_delta_onboarding_fragment) {

    private val viewModel: OnboardingDeltaNotificationsViewModel by viewModels()
    private val binding: NotificationsDeltaOnboardingFragmentBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = viewModel.onProceed()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)

        binding.apply {
            nextButton.setOnClickListener { viewModel.onProceed() }
            toolbar.setNavigationOnClickListener { viewModel.onProceed() }
            deviceSettingsButton.setOnClickListener { onOpenDeviceSettings() }

            faq.setTextWithUrls(
                R.string.nm_faq.toResolvingString(),
                TextViewUrlSet(
                    labelResource = R.string.nm_faq_label,
                    urlResource = R.string.nm_faq_link
                )
            )
        }

        viewModel.routeToScreen.observe(viewLifecycleOwner) {
            when (it) {
                is OnboardingDeltaNotificationsNavigationEvents.CloseScreen ->
                    (requireActivity() as OnboardingActivity).completeOnboarding()
                is OnboardingDeltaNotificationsNavigationEvents.NavigateToOnboardingDeltaAnalyticsFragment ->
                    findNavController().navigate(
                        OnboardingDeltaNotificationsFragmentDirections
                            .actionOnboardingDeltaNotificationsFragmentToOnboardingDeltaAnalyticsFragment()
                    )
            }
        }
    }

    private fun onOpenDeviceSettings() {
        val intent = Intent().apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                }
                else -> {
                    action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    putExtra("app_package", requireContext().packageName)
                    putExtra("app_uid", requireContext().applicationInfo.uid)
                }
            }
        }

        requireContext().startActivity(intent)
    }
}
