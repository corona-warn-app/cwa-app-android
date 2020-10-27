package de.rki.coronawarnapp.test

import dagger.Module
import de.rki.coronawarnapp.test.crash.ui.SettingsCrashReportFragmentModule
import de.rki.coronawarnapp.test.tasks.TaskControllerTestModule

@Module(
    includes = [
        TaskControllerTestModule::class,
        SettingsCrashReportFragmentModule::class
    ]
)
class DeviceForTestersModule
