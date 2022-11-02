package de.rki.coronawarnapp.srs.core.server

import dagger.Reusable
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionPayload
import javax.inject.Inject

@Reusable
class SrsSubmissionServer @Inject constructor() {

    suspend fun submit(payload: SrsSubmissionPayload) {
        // TODO
    }
}
