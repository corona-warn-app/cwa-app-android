package de.rki.coronawarnapp.test.keydownload.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.diagnosiskeys.download.KeyPackageSyncSettings
import de.rki.coronawarnapp.diagnosiskeys.download.KeyPackageSyncTool
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.network.NetworkStateProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.runBlocking

class KeyDownloadTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val syncSettings: KeyPackageSyncSettings,
    networkStateProvider: NetworkStateProvider,
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

    val allowMeteredConnections = syncSettings.allowMeteredConnections.flow.asLiveData()

    val isSyncRunning = MutableLiveData(false)

    fun toggleAllowMeteredConnections() {
        syncSettings.allowMeteredConnections.update { !it }
    }

    fun download() {
        isSyncRunning.postValue(true)
        launch {
            keyPackageSyncTool.syncKeyFiles()
            isSyncRunning.postValue(false)
        }
    }

    fun clearDownloads() {
        isSyncRunning.postValue(true)
        launch {
            keyCacheRepository.clear()
            isSyncRunning.postValue(false)
        }
    }

    fun deleteKeyFile(it: CachedKeyListItem) {
        launch {
            keyCacheRepository.delete(listOf(it.info))
        }
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<KeyDownloadTestFragmentViewModel>
}
