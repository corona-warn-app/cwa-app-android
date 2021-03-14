package de.rki.coronawarnapp.ui.submission.testresult

import de.rki.coronawarnapp.util.DeviceUIState
import de.rki.coronawarnapp.util.NetworkRequestWrapper
import java.util.Date

data class TestResultUIState(
    val deviceUiState: NetworkRequestWrapper<DeviceUIState, Throwable>,
    val testResultReceivedDate: Date?
)
