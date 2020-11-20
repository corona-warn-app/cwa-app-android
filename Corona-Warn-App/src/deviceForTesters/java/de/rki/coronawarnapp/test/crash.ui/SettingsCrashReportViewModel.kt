package de.rki.coronawarnapp.test.crash.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.event.BugEvent
import de.rki.coronawarnapp.bugreporting.reportProblem
import de.rki.coronawarnapp.bugreporting.storage.repository.BugRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class SettingsCrashReportViewModel @AssistedInject constructor(
    private val crashReportRepository: BugRepository
) : CWAViewModel() {

    val crashReports = crashReportRepository.getAll().asLiveData()

    private val selectedCrashReportMutable: MutableLiveData<BugEvent> = MutableLiveData()
    val selectedCrashReport: LiveData<BugEvent> = selectedCrashReportMutable
    val selectedCrashReportFormattedText: LiveData<String> = selectedCrashReportMutable.map {
        createBugEventFormattedText(it)
    }

    fun deleteAllCrashReports() = launch(Dispatchers.IO) {
        crashReportRepository.clear()
    }

    fun simulateException() {
        try {
            val a = 2 / 0
        } catch (e: Exception) {
            Timber.e(e, "Msg: ${e.message}")
            e.reportProblem(SettingsCrashReportViewModel::class.java.simpleName, e.message)
        }
    }

    fun selectCrashReport(bugEvent: BugEvent) {
        selectedCrashReportMutable.postValue(bugEvent)
    }

    private fun createBugEventFormattedText(bugEvent: BugEvent): String =
        "Selected crash report ${bugEvent.id} \n" +
            " # appeared at: ${bugEvent.createdAt} \n\n" +
            " # Device: ${bugEvent.deviceInfo} \n" +
            " # Android Version ${bugEvent.androidVersion} \n" +
            " # Android API-Level ${bugEvent.apiLevel} \n\n" +
            " # AppVersion: ${bugEvent.appVersionName} \n" +
            " # AppVersionCode ${bugEvent.appVersionCode} \n" +
            " # C-Hash ${bugEvent.shortCommitHash} \n\n\n" +
            " ${bugEvent.stackTrace}\n\n" +
            " # Corresponding Log: \n\n ${bugEvent.logHistory}"

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SettingsCrashReportViewModel>
}
