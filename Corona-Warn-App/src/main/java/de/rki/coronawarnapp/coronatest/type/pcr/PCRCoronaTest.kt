package de.rki.coronawarnapp.coronatest.type.pcr

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import java.time.Instant

@Suppress("ConstructorParameterNaming")
data class PCRCoronaTest(
    @JsonProperty("identifier")
    override val identifier: TestIdentifier,

    @JsonProperty("registeredAt")
    override val registeredAt: Instant,

    @JsonProperty("registrationToken")
    override val registrationToken: RegistrationToken,

    @JsonProperty("authCode")
    override val authCode: String? = null,

    @JsonProperty("isSubmitted")
    override val isSubmitted: Boolean = false,

    @JsonProperty("isViewed")
    override val isViewed: Boolean = false,

    @JsonProperty("didShowBadge")
    override val didShowBadge: Boolean = false,

    @JsonProperty("hasResultChangeBadge")
    override val hasResultChangeBadge: Boolean = false,

    @JsonProperty("isAdvancedConsentGiven")
    override val isAdvancedConsentGiven: Boolean = false,

    @JsonProperty("isResultAvailableNotificationSent")
    override val isResultAvailableNotificationSent: Boolean = false,

    @JsonProperty("testResultReceivedAt")
    override val testResultReceivedAt: Instant? = null,

    @JsonProperty("testResult")
    override val testResult: CoronaTestResult,

    @JsonProperty("lastUpdatedAt")
    override val lastUpdatedAt: Instant,

    @JsonIgnore override val isProcessing: Boolean = false,
    @JsonIgnore override val lastError: Throwable? = null,

    @JsonProperty("isDccSupportedByPoc")
    override val isDccSupportedByPoc: Boolean = true,
    @JsonProperty("isDccConsentGiven")
    override val isDccConsentGiven: Boolean = false,

    @JsonProperty("isDccDataSetCreated")
    override val isDccDataSetCreated: Boolean = false,

    @JsonProperty("labId")
    override val labId: String? = null,

    @JsonProperty("qrCodeHash")
    override val qrCodeHash: String? = null,

    @JsonProperty("recycledAt")
    override var recycledAt: Instant? = null,
) : PersonalCoronaTest {
    @get:JsonIgnore
    override val type: BaseCoronaTest.Type
        get() = BaseCoronaTest.Type.PCR
    @get:JsonIgnore
    override val isRedeemed: Boolean
        get() = testResult == CoronaTestResult.PCR_OR_RAT_REDEEMED
    @get:JsonIgnore
    override val isPositive: Boolean
        get() = testResult == CoronaTestResult.PCR_POSITIVE
    @get:JsonIgnore
    override val isNegative: Boolean
        get() = testResult == CoronaTestResult.PCR_NEGATIVE
    @get:JsonIgnore
    override val isPending: Boolean
        get() = testResult == CoronaTestResult.PCR_OR_RAT_PENDING
    @get:JsonIgnore
    override val isInvalid: Boolean
        get() = testResult == CoronaTestResult.PCR_INVALID
    @get:JsonIgnore
    override val isSubmissionAllowed: Boolean
        get() = isPositive && !isSubmitted

    @get:JsonIgnore
    val state: State
        get() = when {
            isRecycled -> State.RECYCLED
            else -> when (testResult) {
                CoronaTestResult.PCR_OR_RAT_PENDING -> State.PENDING
                CoronaTestResult.PCR_NEGATIVE -> State.NEGATIVE
                CoronaTestResult.PCR_POSITIVE -> State.POSITIVE
                CoronaTestResult.PCR_INVALID -> State.INVALID
                CoronaTestResult.PCR_OR_RAT_REDEEMED -> State.REDEEMED
                else -> throw IllegalArgumentException("Invalid PCR test state $testResult")
            }
        }

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
        RECYCLED,
    }
}
