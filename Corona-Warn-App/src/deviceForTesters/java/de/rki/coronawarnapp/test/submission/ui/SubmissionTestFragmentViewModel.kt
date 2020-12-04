package de.rki.coronawarnapp.test.submission.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater.UpdateResult
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.util.UUID

class SubmissionTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val tekHistoryStorage: TEKHistoryStorage,
    private val tekHistoryUpdater: TEKHistoryUpdater
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val errorEvents = SingleLiveEvent<Throwable>()
    private val internalToken = MutableStateFlow(LocalData.registrationToken())
    val currentTestId = internalToken.asLiveData()

    val tekHistory: LiveData<List<TEKHistoryItem>> = tekHistoryStorage.tekData.map { items ->
        items.flatMap { batch ->
            batch.keys
                .map { key ->
                    TEKHistoryItem(
                        obtainedAt = batch.obtainedAt,
                        batchId = batch.batchId,
                        key = key
                    )
                }
                .sortedBy { it.key.rollingStartIntervalNumber }
        }
    }.asLiveData(context = dispatcherProvider.Default)

    init {
        tekHistoryUpdater.callback = object : TEKHistoryUpdater.Callback {
            override fun onTEKAvailable(teks: List<TemporaryExposureKey>) {
                Timber.d("TEKs are available: %s", teks)
            }

            override fun onPermissionDeclined() {
                Timber.d("Permission were declined.")
            }

            override fun onError(error: Throwable) {
                errorEvents.postValue(error)
            }
        }
    }

    fun scrambleRegistrationToken() {
        LocalData.registrationToken(UUID.randomUUID().toString())
        internalToken.value = LocalData.registrationToken()
    }

    fun deleteRegistrationToken() {
        LocalData.registrationToken(null)
        internalToken.value = LocalData.registrationToken()
    }

    fun updateStorage(activity: Activity) {
        tekHistoryUpdater.updateTEKHistoryOrRequestPermission { permissionRequest ->
            permissionRequest.invoke(activity)
        }
    }

    fun clearStorage() {
        launch {
            tekHistoryStorage.clear()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        val result = tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data)
        Timber.d("tekHistoryUpdater.handleActivityResult(): %s", result)

        if (result == UpdateResult.PERMISSION_AVAILABLE) {
            launch {
                try {
                    tekHistoryUpdater.updateHistoryOrThrow()
                } catch (e: Exception) {
                    Timber.e(e, "updateHistoryOrThrow() threw :O")
                    errorEvents.postValue(e)
                }
            }
        }

        return result != UpdateResult.UNKNOWN_RESULT
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestFragmentViewModel>
}
