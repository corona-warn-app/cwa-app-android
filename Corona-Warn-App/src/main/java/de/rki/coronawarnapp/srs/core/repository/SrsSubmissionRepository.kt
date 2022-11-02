package de.rki.coronawarnapp.srs.core.repository

import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import de.rki.coronawarnapp.submission.Symptoms
import javax.inject.Inject

class SrsSubmissionRepository @Inject constructor() {

    suspend fun submit(
        type: SrsSubmissionType,
        symptoms: Symptoms = Symptoms.NO_INFO_GIVEN
    ) {
        // TODO send to server
    }
}
