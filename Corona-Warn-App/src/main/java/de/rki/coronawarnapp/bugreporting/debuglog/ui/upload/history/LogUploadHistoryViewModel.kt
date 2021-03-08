package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.bugreporting.BugReportingSettings
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.LogUpload
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map

class LogUploadHistoryViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    bugReportingSettings: BugReportingSettings
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val logUploads: LiveData<List<LogUpload>> = bugReportingSettings.uploadHistory.flow
        .map { history -> history.logs.sortedByDescending { it.uploadedAt } }
        .asLiveData(context = dispatcherProvider.Default)

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<LogUploadHistoryViewModel>
}
