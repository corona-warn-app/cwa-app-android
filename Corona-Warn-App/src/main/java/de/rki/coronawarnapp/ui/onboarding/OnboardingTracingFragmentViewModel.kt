package de.rki.coronawarnapp.ui.onboarding

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.nearby.TracingPermissionHelper
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.ui.SingleLiveEvent
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class OnboardingTracingFragmentViewModel @AssistedInject constructor(
    private val interoperabilityRepository: InteroperabilityRepository,
    private val tracingPermissionHelper: TracingPermissionHelper
) : CWAViewModel() {

    val countryList = interoperabilityRepository.countryListFlow.asLiveData()
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()
    val permissionRequestEvent = SingleLiveEvent<(Activity) -> Unit>()

    init {
        tracingPermissionHelper.callback = object : TracingPermissionHelper.Callback {
            override fun onUpdateTracingStatus(isTracingEnabled: Boolean) {
                if (isTracingEnabled) {
                    routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingTest)
                }
            }

            override fun onError(error: Throwable) {
                Timber.e(error, "Failed to activate tracing during onboarding.")
            }
        }
    }

    fun saveInteroperabilityUsed() {
        interoperabilityRepository.saveInteroperabilityUsed()
    }

    // Reset tracing state in onboarding
    fun resetTracing() {
        launch {
            try {
                if (InternalExposureNotificationClient.asyncIsEnabled()) {
                    InternalExposureNotificationClient.asyncStop()
                    // Reset initial activation timestamp
                    LocalData.initialTracingActivationTimestamp(0L)
                }
            } catch (exception: Exception) {
                exception.report(
                    ExceptionCategory.EXPOSURENOTIFICATION,
                    TAG,
                    null
                )
            }
        }
    }

    fun onActivateTracingClicked() {
        tracingPermissionHelper.startTracing { permissionRequest ->
            permissionRequestEvent.postValue(permissionRequest)
        }
    }

    fun showCancelDialog() {
        routeToScreen.postValue(OnboardingNavigationEvents.ShowCancelDialog)
    }

    fun onBackButtonPress() {
        routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingPrivacy)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        tracingPermissionHelper.handleActivityResult(requestCode, resultCode, data)
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<OnboardingTracingFragmentViewModel>

    companion object {
        private val TAG: String? = OnboardingTracingFragmentViewModel::class.simpleName
    }
}
