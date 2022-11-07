package de.rki.coronawarnapp.test.submission.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.gson.Gson
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.repository.SrsSubmissionRepository
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber
import kotlin.Exception

class SubmissionTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory,
    timeStamper: TimeStamper,
    @BaseGson baseGson: Gson,
    srsSubmissionSettings: SrsSubmissionSettings,
    private val srsSubmissionRepository: SrsSubmissionRepository,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val exportJson = baseGson.newBuilder().apply {
        setPrettyPrinting()
    }.create()

    val otpData = srsSubmissionSettings.otp.asLiveData2()

    val srsSubmissionResult = SingleLiveEvent<SrsSubmissionResult>()

    private val tekHistoryUpdater = tekHistoryUpdaterFactory.create(
        object : TEKHistoryUpdater.Callback {
            override fun onTEKAvailable(teks: List<TemporaryExposureKey>) {
                Timber.d("TEKs are available: %s", teks)
                val now = timeStamper.nowUTC
                val tekList = teks.map { key ->
                    TEKHistoryItem(
                        obtainedAt = now,
                        key = key
                    )
                }.sortedBy { it.obtainedAt }
                tekHistory.postValue(tekList)
            }

            override fun onTEKPermissionDeclined() {
                Timber.d("Permission were declined.")
            }

            override fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit) {
                showTracingConsentDialog.postValue(onConsentResult)
            }

            override fun onPermissionRequired(permissionRequest: (Activity) -> Unit) {
                permissionRequestEvent.postValue(permissionRequest)
            }

            override fun onError(error: Throwable) {
                errorEvents.postValue(error)
            }
        }
    )

    val errorEvents = SingleLiveEvent<Throwable>()

    val shareTEKsEvent = SingleLiveEvent<TEKExport>()

    val permissionRequestEvent = SingleLiveEvent<(Activity) -> Unit>()
    val showTracingConsentDialog = SingleLiveEvent<(Boolean) -> Unit>()

    val tekHistory = MutableLiveData<List<TEKHistoryItem>>()

    fun submit() = launch {
        try {
            srsSubmissionRepository.submit(SrsSubmissionType.SRS_RAT)
            srsSubmissionResult.postValue(Success)
        } catch (e: Exception) {
            srsSubmissionResult.postValue(Error(e))
            Timber.e(e, "submit()")
        }
    }

    fun updateStorage() {
        tekHistoryUpdater.getTeksForTesting()
    }

    fun emailTEKs() {
        tekHistory.value?.toExportedKeys()?.let {
            launch {
                val tekExport = TEKExport(
                    exportText = exportJson.toJson(it)
                )
                shareTEKsEvent.postValue(tekExport)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return tekHistoryUpdater.handleActivityResult(requestCode, resultCode, data).also {
            Timber.d("tekHistoryUpdater.handleActivityResult(): %s", it)
        }
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestFragmentViewModel>
}

sealed interface SrsSubmissionResult
data class Error(val cause: Exception) : SrsSubmissionResult
object Success : SrsSubmissionResult
