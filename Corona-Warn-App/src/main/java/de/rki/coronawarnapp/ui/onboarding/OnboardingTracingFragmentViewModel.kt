package de.rki.coronawarnapp.ui.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.interoperability.InteroperabilityRepository
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.launch

class OnboardingTracingFragmentViewModel @AssistedInject constructor(
    private val interoperabilityRepository: InteroperabilityRepository
) : CWAViewModel() {

    val countryList = MutableLiveData(interoperabilityRepository.getAllCountries())

    fun saveInteroperabilityUsed() {
        interoperabilityRepository.saveInteroperabilityUsed()
    }

    // Reset tracing state in onboarding
    fun resetTracing() {
        viewModelScope.launch {
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

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<OnboardingTracingFragmentViewModel>

    companion object {
        private val TAG: String? = OnboardingTracingFragmentViewModel::class.simpleName
    }
}
