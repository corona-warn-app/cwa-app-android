package de.rki.coronawarnapp.ui.settings.crash

import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.crash.CrashReportEntity
import de.rki.coronawarnapp.crash.CrashReportRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsCrashReportViewModel @AssistedInject constructor(
    private val crashReportRepository: CrashReportRepository
) : CWAViewModel() {

    val crashReports = crashReportRepository.allCrashReports

    var selectedCrashReport: CrashReportEntity? = null

    fun deleteAllCrashReports() = viewModelScope.launch(Dispatchers.IO) {
        selectedCrashReport = null
        crashReportRepository.deleteAllCrashReports()
    }

    fun simulateExceptioin() = viewModelScope.launch(Dispatchers.IO) {
        // toDo just for testing, remove later
        try {
            val a = 2 / 0
        } catch (e: Exception) {
            Timber.e(e, "Msg: ${e.message}")
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsCrashReportViewModel>
}
