package de.rki.coronawarnapp.coronatest.type

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import org.joda.time.Instant

interface CoronaTest {
    val identifier: TestIdentifier
    val type: Type

    val registeredAt: Instant
    val registrationToken: RegistrationToken

    val isProcessing: Boolean
    val lastError: Throwable?

    val isSubmissionAllowed: Boolean
    val isSubmitted: Boolean
    val isViewed: Boolean

    val testResultReceivedAt: Instant?
    val testResult: CoronaTestResult

    // TODO why do we need this PER test
    val isAdvancedConsentGiven: Boolean

    // TODO Why do we need to store this?
    val isJournalEntryCreated: Boolean

    val isResultAvailableNotificationSent: Boolean

    enum class Type(val raw: String) {
        @SerializedName("PCR")
        PCR("PCR"),

        @SerializedName("RAPID_ANTIGEN")
        RAPID_ANTIGEN("RAPID_ANTIGEN"),
    }
}

typealias RegistrationToken = String
typealias TestIdentifier = String
