package de.rki.coronawarnapp.test.crash

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.bugreporting.storage.repository.BugRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsCrashReportViewModel @AssistedInject constructor(
    private val crashReportRepository: BugRepository
) : CWAViewModel() {

    val crashReports = crashReportRepository.getAll()

    lateinit var selectedCrashReport: LiveData<BugEvent>

    fun deleteAllCrashReports() = viewModelScope.launch(Dispatchers.IO) {
        crashReportRepository.clear()
    }

    fun simulateExceptioin() {
        try {
            val a = 2 / 0
        } catch (e: Exception) {
            Timber.e(e, "Msg: ${e.message}")
        }
    }

    fun selectCrashReport(id: Long) {
        Timber.d("Selected crash report $id")
        selectedCrashReport = crashReportRepository.get(id)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsCrashReportViewModel>
}
