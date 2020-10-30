package de.rki.coronawarnapp.ui.submission.testresult

import de.rki.coronawarnapp.ui.submission.ApiRequestState
import de.rki.coronawarnapp.util.DeviceUIState
import java.util.Date

data class TestResultUIState(
    val apiRequestState: ApiRequestState,
    val deviceUiState: DeviceUIState,
    val testResultReceivedDate: Date?
)
