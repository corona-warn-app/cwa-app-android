package de.rki.coronawarnapp.srs.ui.typeselection

import androidx.annotation.StringRes
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType

data class SrsTypeSelectionItem(
    val index: Int,
    @StringRes val buttonText: Int,
    val submissionType: SrsSubmissionType?
)
