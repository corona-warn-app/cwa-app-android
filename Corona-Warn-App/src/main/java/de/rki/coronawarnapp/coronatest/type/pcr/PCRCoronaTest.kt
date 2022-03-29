package de.rki.coronawarnapp.coronatest.type.pcr

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import org.joda.time.Instant

@Suppress("ConstructorParameterNaming")
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

    @SerializedName("didShowBadge")
    override val didShowBadge: Boolean = false,

    @SerializedName("hasResultChangeBadge")
    override val hasResultChangeBadge: Boolean = false,

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

    @SerializedName("isDccSupportedByPoc")
    private val _isDccSupportedByPoc: Boolean? = true,
    @SerializedName("isDccConsentGiven")
    override val isDccConsentGiven: Boolean = false,

    @SerializedName("isDccDataSetCreated")
    override val isDccDataSetCreated: Boolean = false,

    @SerializedName("labId")
    override val labId: String? = null,

    @SerializedName("qrCodeHash")
    override val qrCodeHash: String? = null,

    @SerializedName("recycledAt")
    override var recycledAt: Instant? = null,
) : PersonalCoronaTest {

    override val type: BaseCoronaTest.Type
        get() = BaseCoronaTest.Type.PCR

    override val isRedeemed: Boolean
        get() = testResult == CoronaTestResult.PCR_OR_RAT_REDEEMED

    override val isPositive: Boolean
        get() = testResult == CoronaTestResult.PCR_POSITIVE

    override val isNegative: Boolean
        get() = testResult == CoronaTestResult.PCR_NEGATIVE

    override val isPending: Boolean
        get() = testResult == CoronaTestResult.PCR_OR_RAT_PENDING

    override val isSubmissionAllowed: Boolean
        get() = isPositive && !isSubmitted

    // Set to true for old records
    override val isDccSupportedByPoc: Boolean
        get() = _isDccSupportedByPoc ?: true

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
