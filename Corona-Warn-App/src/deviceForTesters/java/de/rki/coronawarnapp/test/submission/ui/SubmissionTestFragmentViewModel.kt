package de.rki.coronawarnapp.test.submission.ui

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.gson.Gson
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.main.CWASettings
import de.rki.coronawarnapp.srs.core.AndroidIdProvider
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionResponse
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.srs.core.repository.SrsSubmissionRepository
import de.rki.coronawarnapp.srs.core.storage.SrsDevSettings
import de.rki.coronawarnapp.srs.core.storage.SrsSubmissionSettings
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.serialization.BaseGson
import de.rki.coronawarnapp.util.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber
import java.time.Instant

class SubmissionTestFragmentViewModel @AssistedInject constructor(
    @BaseGson baseGson: Gson,
    timeStamper: TimeStamper,
    androidIdProvider: AndroidIdProvider,
    dispatcherProvider: DispatcherProvider,
    tekHistoryUpdaterFactory: TEKHistoryUpdater.Factory,
    private val srsDevSettings: SrsDevSettings,
    private val appConfigProvider: AppConfigProvider,
    private val srsSubmissionSettings: SrsSubmissionSettings,
    private val srsSubmissionRepository: SrsSubmissionRepository,
    private val cwaSettings: CWASettings,
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val srsSubmissionResult = SingleLiveEvent<SrsSubmissionResult>()

    val otpData = srsSubmissionSettings.otp.asLiveData2()
    val mostRecentSubmissionDate = srsSubmissionSettings.mostRecentSubmissionTime.asLiveData2()

    val androidId = liveData { emit(androidIdProvider.getAndroidId()) }
    val deviceTimeState = srsDevSettings.deviceTimeState.asLiveData2()
    val checkLocalPrerequisites = srsDevSettings.checkLocalPrerequisites.asLiveData2()
    val forceAndroidIdAcceptance = srsDevSettings.forceAndroidIdAcceptance.asLiveData2()
    val firstReliableTime = cwaSettings.firstReliableDeviceTime.asLiveData2()

    private val exportJson = baseGson.newBuilder().apply { setPrettyPrinting() }.create()

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
            val result = srsSubmissionRepository.submit(
                type = SrsSubmissionType.SRS_SELF_TEST,
                keys = tekHistory.value.orEmpty().map { it.key }
            )

            val event = when (result) {
                SrsSubmissionResponse.Success -> Success
                is SrsSubmissionResponse.TruncatedKeys -> TruncatedSubmission(result.days)
            }
            srsSubmissionResult.postValue(event)
        } catch (e: Exception) {
            Timber.e(e, "submit()")
            srsSubmissionResult.postValue(Error(e))
        }
    }

    fun resetMostRecentSubmission() = launch {
        srsSubmissionSettings.resetMostRecentSubmission()
    }

    fun resetOtp() = launch {
        srsSubmissionSettings.resetOtp()
    }

    fun checkLocalPrerequisites(check: Boolean) = launch {
        srsDevSettings.checkLocalPrerequisites(check)
    }

    fun forceAndroidIdAcceptance(force: Boolean) = launch {
        srsDevSettings.forceAndroidIdAcceptance(force)
    }

    fun deviceTimeState(state: ConfigData.DeviceTimeState?) = launch {
        srsDevSettings.deviceTimeState(state)
        appConfigProvider.apply {
            reset()
            getAppConfig()
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

    fun clearTekCache() = launch {
        tekHistoryUpdater.clearTekCache()
    }

    fun updateFirstReliableTime(time: Long) = launch {
        cwaSettings.updateFirstReliableDeviceTime(Instant.ofEpochMilli(time))
    }

    @AssistedFactory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestFragmentViewModel>
}

sealed interface SrsSubmissionResult

data class TruncatedSubmission(val numberOfDays: String?) : SrsSubmissionResult
data class Error(val cause: Exception) : SrsSubmissionResult
object Success : SrsSubmissionResult
