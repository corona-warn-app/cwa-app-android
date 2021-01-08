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
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import timber.log.Timber

class OnboardingTracingFragmentViewModel @AssistedInject constructor(
    private val interoperabilityRepository: InteroperabilityRepository,
    tracingPermissionHelperFactory: TracingPermissionHelper.Factory,
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    val countryList = interoperabilityRepository.countryList
        .asLiveData(context = dispatcherProvider.Default)
    val routeToScreen: SingleLiveEvent<OnboardingNavigationEvents> = SingleLiveEvent()
    val permissionRequestEvent = SingleLiveEvent<(Activity) -> Unit>()

    private val tracingPermissionHelper =
        tracingPermissionHelperFactory.create(object : TracingPermissionHelper.Callback {
            override fun onUpdateTracingStatus(isTracingEnabled: Boolean) {
                if (isTracingEnabled) {
                    routeToScreen.postValue(OnboardingNavigationEvents.NavigateToOnboardingTest)
                }
            }

            override fun onTracingConsentRequired(onConsentResult: (given: Boolean) -> Unit) {
                // Tracing consent is given implicitly on this screen.
                onConsentResult(true)
            }

            override fun onPermissionRequired(permissionRequest: (Activity) -> Unit) {
                permissionRequestEvent.postValue(permissionRequest)
            }

            override fun onError(error: Throwable) {
                Timber.e(error, "Failed to activate tracing during onboarding.")
            }
        })

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
        tracingPermissionHelper.startTracing()
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
