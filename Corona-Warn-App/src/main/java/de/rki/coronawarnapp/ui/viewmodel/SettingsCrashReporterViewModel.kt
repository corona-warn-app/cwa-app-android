package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import de.rki.coronawarnapp.crash.CrashReportRepository
import de.rki.coronawarnapp.util.di.AppInjector

class SettingsCrashReporterViewModel : ViewModel() {

    // TODO: Remove this after updating branch
    var crashReportRepository: CrashReportRepository = AppInjector.component.crashReportRepository

    val crashReports = crashReportRepository.allCrashReports
}
