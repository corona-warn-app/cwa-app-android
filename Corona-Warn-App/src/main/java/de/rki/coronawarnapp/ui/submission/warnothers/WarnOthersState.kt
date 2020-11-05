package de.rki.coronawarnapp.ui.submission.warnothers

import de.rki.coronawarnapp.ui.Country
import de.rki.coronawarnapp.ui.submission.ApiRequestState

data class WarnOthersState(
    val apiRequestState: ApiRequestState,
    val countryList: List<Country>
) {

    fun isSubmitButtonEnabled(): Boolean =
        apiRequestState == ApiRequestState.IDLE || apiRequestState == ApiRequestState.FAILED

    fun isSubmitSpinnerVisible(): Boolean {
        return apiRequestState == ApiRequestState.STARTED
    }
}
