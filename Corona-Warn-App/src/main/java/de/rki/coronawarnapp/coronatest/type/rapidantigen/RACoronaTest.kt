package de.rki.coronawarnapp.coronatest.type.rapidantigen

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import java.time.Instant
import java.time.LocalDate

data class RACoronaTest(
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

    @JsonProperty("lastUpdatedAt")
    override val lastUpdatedAt: Instant,

    @JsonProperty("testResult")
    override val testResult: CoronaTestResult,

    @JsonProperty("testedAt")
    val testedAt: Instant,

    @JsonProperty("firstName")
    val firstName: String? = null,

    @JsonProperty("lastName")
    val lastName: String? = null,

    @JsonProperty("dateOfBirth")
    val dateOfBirth: LocalDate? = null,

    @JsonProperty("sampleCollectedAt")
    val sampleCollectedAt: Instant? = null,

    @JsonIgnore override val isProcessing: Boolean = false,
    @JsonIgnore override val lastError: Throwable? = null,

    @JsonProperty("isDccSupportedByPoc")
    override val isDccSupportedByPoc: Boolean = false,
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
        get() = BaseCoronaTest.Type.RAPID_ANTIGEN

    private fun isOutdated(nowUTC: Instant, testConfig: CoronaTestConfig): Boolean =
        testTakenAt.plus(testConfig.ratParameters.hoursToDeemTestOutdated).isBefore(nowUTC)

    fun getState(nowUTC: Instant, testConfig: CoronaTestConfig) = when {
        isRecycled -> State.RECYCLED
        testResult == RAT_NEGATIVE && isOutdated(nowUTC, testConfig) -> State.OUTDATED
        else -> when (testResult) {
            PCR_OR_RAT_PENDING,
            RAT_PENDING -> State.PENDING
            RAT_NEGATIVE -> State.NEGATIVE
            RAT_POSITIVE -> State.POSITIVE
            RAT_INVALID -> State.INVALID
            PCR_OR_RAT_REDEEMED,
            RAT_REDEEMED -> State.REDEEMED
            else -> throw IllegalArgumentException("Invalid RAT test state $testResult")
        }
    }
    @get:JsonIgnore
    val testTakenAt: Instant
        get() = sampleCollectedAt ?: testedAt
    @get:JsonIgnore
    override val isRedeemed: Boolean
        get() = testResult == PCR_OR_RAT_REDEEMED || testResult == RAT_REDEEMED
    @get:JsonIgnore
    override val isPositive: Boolean
        get() = testResult == RAT_POSITIVE
    @get:JsonIgnore
    override val isNegative: Boolean
        get() = testResult == RAT_NEGATIVE
    @get:JsonIgnore
    override val isPending: Boolean
        get() = setOf(PCR_OR_RAT_PENDING, RAT_PENDING).contains(testResult)
    @get:JsonIgnore
    override val isInvalid: Boolean
        get() = testResult == RAT_INVALID
    @get:JsonIgnore
    override val isSubmissionAllowed: Boolean
        get() = isPositive && !isSubmitted

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
        OUTDATED,
        RECYCLED,
    }
}
