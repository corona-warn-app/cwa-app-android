package de.rki.coronawarnapp.srs.ui.typeselection

import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType

val typeList = listOf(
    SrsTypeSelectionItem(
        0,
        R.string.target_button_rat_registered_no_result_text,
        SrsSubmissionType.SRS_RAT
    ),
    SrsTypeSelectionItem(
        1,
        R.string.target_button_rat_not_registered_text,
        SrsSubmissionType.SRS_RAT
    ),
    SrsTypeSelectionItem(
        2,
        R.string.target_button_pcr_registered_no_result_text,
        SrsSubmissionType.SRS_REGISTERED_PCR
    ),
    SrsTypeSelectionItem(
        3,
        R.string.target_button_pcr_not_registered_text,
        SrsSubmissionType.SRS_UNREGISTERED_PCR
    ),
    SrsTypeSelectionItem(
        4,
        R.string.target_button_rapid_pcr_text,
        SrsSubmissionType.SRS_RAPID_PCR
    ),
    SrsTypeSelectionItem(
        5,
        R.string.target_button_other_text,
        SrsSubmissionType.SRS_OTHER
    )
)
