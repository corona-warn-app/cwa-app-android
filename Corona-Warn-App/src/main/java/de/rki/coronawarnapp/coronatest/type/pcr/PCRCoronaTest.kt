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

    @SerializedName("isResultAvailableNotificationSent")
    override val isResultAvailableNotificationSent: Boolean = false,

    @SerializedName("testResultReceivedAt")
    override val testResultReceivedAt: Instant? = null,

    @SerializedName("testResult")
    override val testResult: CoronaTestResult,

    @SerializedName("lastUpdatedAt")
    override val lastUpdatedAt: Instant,

    @Transient override val isProcessing: Boolean = false,
    @Transient override val lastError: Throwable? = null,

    @SerializedName("isDccConsentGiven")
    override val isDccConsentGiven: Boolean = false,

    @SerializedName("isDccDataSetCreated")
    override val isDccDataSetCreated: Boolean = false,
) : CoronaTest {

    override val type: CoronaTest.Type
        get() = CoronaTest.Type.PCR

    override val isFinal: Boolean
        get() = testResult == CoronaTestResult.PCR_REDEEMED

    override val isPositive: Boolean
        get() = testResult == CoronaTestResult.PCR_POSITIVE

    override val isNegative: Boolean
        get() = testResult == CoronaTestResult.PCR_NEGATIVE

    override val isPending: Boolean
        get() = testResult == CoronaTestResult.PCR_OR_RAT_PENDING

    override val isSubmissionAllowed: Boolean
        get() = isPositive && !isSubmitted

    val state: State
        get() = when (testResult) {
            CoronaTestResult.PCR_OR_RAT_PENDING -> State.PENDING
            CoronaTestResult.PCR_NEGATIVE -> State.NEGATIVE
            CoronaTestResult.PCR_POSITIVE -> State.POSITIVE
            CoronaTestResult.PCR_INVALID -> State.INVALID
            CoronaTestResult.PCR_REDEEMED -> State.REDEEMED
            else -> throw IllegalArgumentException("Invalid PCR test state $testResult")
        }

    override val isDccSupportedByPoc: Boolean
        get() = true

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
    }
}
