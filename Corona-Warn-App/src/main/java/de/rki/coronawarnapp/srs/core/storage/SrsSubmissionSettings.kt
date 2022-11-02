package de.rki.coronawarnapp.srs.core.storage

import de.rki.coronawarnapp.srs.core.model.SrsOtp
import java.time.Instant
import javax.inject.Inject

class SrsSubmissionSettings @Inject constructor() {
    suspend fun getMostRecentSubmissionTime(): Instant {
        return Instant.EPOCH
    }

    suspend fun getOtp(): SrsOtp? {
        @Suppress("FunctionOnlyReturningConstant")
        return null
    }
}
