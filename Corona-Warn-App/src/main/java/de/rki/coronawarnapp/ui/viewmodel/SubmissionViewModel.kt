package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel
import de.rki.coronawarnapp.util.viewmodel.InjectedSubmissionViewModelFactory

class SubmissionViewModel @AssistedInject constructor(
    @Assisted submissionRepository: SubmissionRepository
) : CWAViewModel() {

    val deviceUiState: LiveData<DeviceUIState> = submissionRepository.deviceUIStateFlow.asLiveData()

    @AssistedInject.Factory
    interface Factory : InjectedSubmissionViewModelFactory<SubmissionViewModel>
}
