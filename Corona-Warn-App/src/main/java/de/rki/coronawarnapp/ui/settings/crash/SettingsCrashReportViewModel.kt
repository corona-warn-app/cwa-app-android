package de.rki.coronawarnapp.ui.settings.crash

import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.crash.CrashReportRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsCrashReportViewModel @AssistedInject constructor(
    private val crashReportRepository: CrashReportRepository
) : CWAViewModel() {

    val crashReports = crashReportRepository.allCrashReports

    fun deleteAllCrashReports() = viewModelScope.launch(Dispatchers.IO) {
        crashReportRepository.deleteAllCrashReports()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsCrashReportViewModel>
}
