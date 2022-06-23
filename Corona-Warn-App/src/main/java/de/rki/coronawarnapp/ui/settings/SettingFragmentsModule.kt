package de.rki.coronawarnapp.ui.settings

import dagger.Module
import de.rki.coronawarnapp.tracing.ui.settings.TracingSettingsFragmentModule
import de.rki.coronawarnapp.ui.settings.analytics.SettingsPrivacyPreservingAnalyticsFragmentModule
import de.rki.coronawarnapp.ui.settings.backgroundpriority.SettingsBackgroundPriorityFragmentModule
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsFragmentModule
import de.rki.coronawarnapp.ui.settings.start.SettingsFragmentModule

@Module(
    includes = [
        SettingsFragmentModule::class,
        TracingSettingsFragmentModule::class,
        NotificationSettingsFragmentModule::class,
        SettingsBackgroundPriorityFragmentModule::class,
        SettingsPrivacyPreservingAnalyticsFragmentModule::class
    ]
)
class SettingFragmentsModule
