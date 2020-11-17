package de.rki.coronawarnapp.test.submission.ui

import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.SimpleCWAViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class SubmissionTestFragmentViewModel @AssistedInject constructor(
    dispatcherProvider: DispatcherProvider
) : CWAViewModel(dispatcherProvider = dispatcherProvider) {

    private val internalToken = MutableStateFlow(LocalData.registrationToken())
    val currentTestId = internalToken.asLiveData()

    fun scrambleRegistrationToken() {
        LocalData.registrationToken(UUID.randomUUID().toString())
        internalToken.value = LocalData.registrationToken()
    }

    fun deleteRegistrationToken() {
        LocalData.registrationToken(null)
        internalToken.value = LocalData.registrationToken()
    }

    @AssistedInject.Factory
    interface Factory : SimpleCWAViewModelFactory<SubmissionTestFragmentViewModel>
}
