package de.rki.coronawarnapp.bugreporting.debuglog.ui.upload.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage.UploadHistoryStorage
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class LogUploadHistoryViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    uploadHistoryStorage: UploadHistoryStorage
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val logUploads: LiveData<List<LogUpload>> = uploadHistoryStorage.uploadHistory
        .map { history -> history.logs.sortedByDescending { it.uploadedAt } }
        .asLiveData(context = dispatcherProvider.Default)
}
