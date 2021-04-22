package de.rki.coronawarnapp.coronatest.type.pcr

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import org.joda.time.Instant

data class PCRCoronaTest(
    @SerializedName("identifier")
    override val identifier: TestIdentifier,

    @SerializedName("registeredAt")
    override val registeredAt: Instant,

    @SerializedName("registrationToken")
    override val registrationToken: RegistrationToken,

    @SerializedName("isSubmitted")
    override val isSubmitted: Boolean = false,

    @SerializedName("isViewed")
    override val isViewed: Boolean = false,

    @SerializedName("isAdvancedConsentGiven")
    override val isAdvancedConsentGiven: Boolean = false,

    @SerializedName("isJournalEntryCreated")
    override val isJournalEntryCreated: Boolean = false,

    @SerializedName("isResultAvailableNotificationSent")
    override val isResultAvailableNotificationSent: Boolean = false,

    @SerializedName("testResultReceivedAt")
    override val testResultReceivedAt: Instant? = null,

    @SerializedName("testResult")
    override val testResult: CoronaTestResult,

    @Transient override val isProcessing: Boolean = false,
    @Transient override val lastError: Throwable? = null,
) : CoronaTest {

    @Transient
    override val type: CoronaTest.Type = CoronaTest.Type.PCR

    @Transient
    override val isPositive: Boolean = testResult == CoronaTestResult.PCR_POSITIVE

    @Transient
    override val isPending: Boolean = testResult == CoronaTestResult.PCR_OR_RAT_PENDING

    @Transient
    override val isSubmissionAllowed: Boolean = isPositive && !isSubmitted

    @Transient
    val state: State = when (testResult) {
        CoronaTestResult.PCR_OR_RAT_PENDING -> State.PENDING
        CoronaTestResult.PCR_NEGATIVE -> State.NEGATIVE
        CoronaTestResult.PCR_POSITIVE -> State.POSITIVE
        CoronaTestResult.PCR_INVALID -> State.INVALID
        CoronaTestResult.PCR_REDEEMED -> State.REDEEMED
        else -> throw IllegalArgumentException("Invalid PCR test state $testResult")
    }

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
    }
}
