package de.rki.coronawarnapp.util

import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_ERROR
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_NO_RESULT
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE
import de.rki.coronawarnapp.util.DeviceUIState.PAIRED_POSITIVE_TELETAN
import de.rki.coronawarnapp.util.DeviceUIState.SUBMITTED_FINAL
import de.rki.coronawarnapp.util.DeviceUIState.UNPAIRED

object UiStateHelper {
    fun uiState(): DeviceUIState {
        return PAIRED_POSITIVE

        var uiState = UNPAIRED
        if (LocalData.registrationToken() != "") {
            if (LocalData.inititalTestResultReceivedTimestamp() == null) {
                uiState = PAIRED_NO_RESULT
            } else if (LocalData.isAllowedToSubmitDiagnosisKeys() == true) {
                uiState = PAIRED_POSITIVE
            }
        } else if (LocalData.numberOfSuccessfulSubmissions() == 1) {
            uiState = SUBMITTED_FINAL
        }

        return uiState
    }
}
