package de.rki.coronawarnapp.ui.settings

import dagger.Module
import de.rki.coronawarnapp.ui.settings.crash.SettingsCrashReportFragmentModule
import de.rki.coronawarnapp.ui.settings.notifications.NotificationSettingsFragmentModule
import de.rki.coronawarnapp.ui.settings.start.SettingsFragmentModule
import de.rki.coronawarnapp.ui.tracing.settings.SettingsTracingFragmentModule

@Module(
    includes = [
        SettingsFragmentModule::class,
        SettingsTracingFragmentModule::class,
        NotificationSettingsFragmentModule::class,
        SettingsCrashReportFragmentModule::class
    ]
)
class SettingFragmentsModule
