package de.rki.coronawarnapp.srs.ui.typeselection

import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType

data class SrsTypeSelectionItem(
    val checked: Boolean = false,
    val submissionType: SrsSubmissionType
)
