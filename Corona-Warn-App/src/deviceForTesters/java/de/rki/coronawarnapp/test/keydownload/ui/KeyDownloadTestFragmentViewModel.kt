package de.rki.coronawarnapp.test.keydownload.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import de.rki.coronawarnapp.diagnosiskeys.download.KeyPackageSyncTool
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.storage.TestSettings
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class KeyDownloadTestFragmentViewModel @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    networkStateProvider: NetworkStateProvider,
    private val testSettings: TestSettings,
    private val keyPackageSyncTool: KeyPackageSyncTool,
    private val keyCacheRepository: KeyCacheRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val currentCache = runBlocking {
        // TODO runBlocking is not nice, how can we solve this better?
        keyCacheRepository
            .allCachedKeys()
            .sample(250)
            .map { items ->
                items
                    .sortedWith(compareBy({ it.info.day }, { it.info.hour }))
                    .reversed()
                    .map { CachedKeyListItem(it.info, it.path.length()) }
            }
            .asLiveData()
    }

    val isMeteredConnection = networkStateProvider.networkState
        .map { it.isMeteredConnection }
        .asLiveData()

    val fakeMeteredConnection = testSettings.fakeMeteredConnection.asLiveData()

    val isSyncRunning = MutableLiveData(false)
    val errorEvent = SingleLiveEvent<Exception>()

    fun toggleAllowMeteredConnections() = launch {
        testSettings.updateFakeMeteredConnection { !it }
    }

    fun download() = launchWithSyncProgress {
        keyPackageSyncTool.syncKeyFiles()
    }

    fun clearDownloads() = launchWithSyncProgress { keyCacheRepository.reset() }

    private fun launchWithSyncProgress(action: suspend () -> Unit) {
        isSyncRunning.postValue(true)
        launch {
            try {
                action()
            } catch (e: Exception) {
                Timber.e(e, "Call failed.")
                errorEvent.postValue(e)
            } finally {
                isSyncRunning.postValue(false)
            }
        }
    }

    fun deleteKeyFile(it: CachedKeyListItem) = launchWithSyncProgress {
        keyCacheRepository.deleteInfoAndFile(listOf(it.info))
    }
}
