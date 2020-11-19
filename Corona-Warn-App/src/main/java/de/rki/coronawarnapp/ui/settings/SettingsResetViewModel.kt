package de.rki.coronawarnapp.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.notification.TestResultNotificationService
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.RiskLevelRepository
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.security.SecurityHelper
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.InjectedSubmissionViewModelFactory
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class SettingsResetViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider,
    private val testResultNotificationService: TestResultNotificationService,
    @AppContext private val context: Context,
    private val keyCacheRepository: KeyCacheRepository,
    private val appConfigProvider: AppConfigProvider,
    private val interoperabilityRepository: InteroperabilityRepository,
    @Assisted private val submissionRepository: SubmissionRepository
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val clickEvent: SingleLiveEvent<SettingsEvents> = SingleLiveEvent()

    fun resetAllData() {
        clickEvent.postValue(SettingsEvents.ResetApp)
    }

    fun goBack() {
        clickEvent.postValue(SettingsEvents.GoBack)
    }

    fun deleteAllAppContent() {
        launch {
            try {
                val isTracingEnabled = InternalExposureNotificationClient.asyncIsEnabled()
                // only stop tracing if it is currently enabled
                if (isTracingEnabled) {
                    InternalExposureNotificationClient.asyncStop()
                    BackgroundWorkScheduler.stopWorkScheduler()
                }
            } catch (apiException: ApiException) {
                apiException.report(
                    ExceptionCategory.EXPOSURENOTIFICATION, TAG, null
                )
            }
            testResultNotificationService.resetPositiveTestResultNotification()

            clearAllLocalData()
            clickEvent.postValue(SettingsEvents.GoToOnboarding)
        }
    }

    private val mutex = Mutex()
    /**
     * Deletes all data known to the Application
     *
     */
    @SuppressLint("ApplySharedPref") // We need a commit here to ensure consistency
    suspend fun clearAllLocalData() = mutex.withLock {
        Timber.w("CWA LOCAL DATA DELETION INITIATED.")
        // Database Reset
        AppDatabase.reset(context)
        // Shared Preferences Reset
        SecurityHelper.resetSharedPrefs()
        // Reset the current risk level stored in LiveData
        RiskLevelRepository.reset()
        // Reset the current states stored in LiveData
        submissionRepository.resetUiState()
        keyCacheRepository.clear()
        appConfigProvider.clear()
        interoperabilityRepository.clear()
        Timber.w("CWA LOCAL DATA DELETION COMPLETED.")
    }

    companion object {
        private val TAG: String? = SettingsResetFragment::class.simpleName
    }

    @AssistedInject.Factory
    interface Factory : InjectedSubmissionViewModelFactory<SettingsResetViewModel>
}
