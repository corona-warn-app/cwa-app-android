package de.rki.coronawarnapp.ui.settings

import dagger.Module
import de.rki.coronawarnapp.tracing.ui.settings.SettingsTracingFragmentModule
import de.rki.coronawarnapp.ui.settings.backgroundpriority.SettingsBackgroundPriorityFragmentModule
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsFragmentModule
import de.rki.coronawarnapp.ui.settings.start.SettingsFragmentModule

@Module(
    includes = [
        SettingsFragmentModule::class,
        SettingsTracingFragmentModule::class,
        NotificationSettingsFragmentModule::class,
        SettingsBackgroundPriorityFragmentModule::class
    ]
)
class SettingFragmentsModule
