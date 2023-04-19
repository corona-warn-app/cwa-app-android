package de.rki.coronawarnapp.ui.settings.notifications

import TextViewUrlSet
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.FragmentSettingsNotificationsBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openAppNotificationSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.toResolvingString
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import setTextWithUrls
import javax.inject.Inject

/**
 * This is the setting notification page. Here the user sees his os notifications settings status.
 * He can navigate to system notification settings and enable/disable them with one click.
 */
class NotificationSettingsFragment :
    Fragment(R.layout.fragment_settings_notifications),
    AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: NotificationSettingsFragmentViewModel by cwaViewModels { viewModelFactory }

    private val binding: FragmentSettingsNotificationsBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            viewModel.notificationSettingsState.observe(viewLifecycleOwner) {
                informationDetailsHeaderIllustration.apply {
                    setImageResource(it.getNotificationsImage())
                    contentDescription = it.getNotificationsIllustrationText(requireContext())
                }
                notificationStateValue.setText(it.getNotificationStatusText())
                notificationStateHeader.setText(it.getNotificationsHeader())
                notificationEnabledViews.isGone = !it.isNotificationsEnabled
                notificationDisabledViews.isGone = it.isNotificationsEnabled
            }

            openSystemSettingsButton.setOnClickListener {
                viewModel.createNotificationChannels()
                requireContext().openAppNotificationSettings()
            }

            settingsNotificationsHeader.setNavigationOnClickListener { popBackStack() }

            faq.setTextWithUrls(
                R.string.nm_faq.toResolvingString(),
                TextViewUrlSet(
                    labelResource = R.string.nm_faq_label,
                    urlResource = R.string.nm_faq_link
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        binding.settingsNotificationsContainer.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
