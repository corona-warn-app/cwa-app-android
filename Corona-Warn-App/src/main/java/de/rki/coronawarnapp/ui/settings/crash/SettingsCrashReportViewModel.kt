package de.rki.coronawarnapp.ui.settings.crash

import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.crash.CrashReportRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory

class SettingsCrashReportViewModel @AssistedInject constructor(
    private val crashReportRepository: CrashReportRepository
) : CWAViewModel() {

    val crashReports = crashReportRepository.allCrashReports

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsCrashReportViewModel>
}
