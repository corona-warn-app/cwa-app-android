package de.rki.coronawarnapp.coronatest.type

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import org.joda.time.Instant

interface CoronaTest {
    val testGUID: CoronaTestGUID
    val type: Type

    val registeredAt: Instant
    val registrationToken: RegistrationToken

    val isProcessing: Boolean
    val lastError: Throwable?

    val isSubmissionAllowed: Boolean
    val isSubmitted: Boolean
    val isViewed: Boolean

    // TODO why do we need this PER test
    val isAdvancedConsentGiven: Boolean

    // TODO Why do we need to store this?
    val isJournalEntryCreated: Boolean

    val isNotificationSent: Boolean

    enum class Type {
        @SerializedName("PCR")
        PCR,

        @SerializedName("RAPID_ANTIGEN")
        RAPID_ANTIGEN,
    }
}

typealias RegistrationToken = String
