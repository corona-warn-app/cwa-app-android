package de.rki.coronawarnapp.ui.submission.warnothers

import de.rki.coronawarnapp.task.TaskState
import de.rki.coronawarnapp.ui.Country

data class WarnOthersState(
    val submitTaskState: TaskState?,
    val countryList: List<Country>
) {

    fun isSubmitButtonEnabled(): Boolean =
        submitTaskState == null || submitTaskState.isFailed

    fun isSubmitSpinnerVisible(): Boolean =
        submitTaskState != null && submitTaskState.isActive
}
