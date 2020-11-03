package de.rki.coronawarnapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.viewmodel.CWAViewModel

class SubmissionViewModel : CWAViewModel() {

    val deviceUiState: LiveData<DeviceUIState> = SubmissionRepository.deviceUIStateFlow.asLiveData()
}
