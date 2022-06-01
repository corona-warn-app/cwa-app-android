package de.rki.coronawarnapp.coronatest.type.rapidantigen

import com.google.gson.annotations.SerializedName
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
import org.joda.time.Instant
import org.joda.time.LocalDate

data class RACoronaTest(
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

    @SerializedName("lastUpdatedAt")
    override val lastUpdatedAt: Instant,

    @SerializedName("testResult")
    override val testResult: CoronaTestResult,

    @SerializedName("testedAt")
    val testedAt: Instant,

    @SerializedName("firstName")
    val firstName: String? = null,

    @SerializedName("lastName")
    val lastName: String? = null,

    @SerializedName("dateOfBirth")
    val dateOfBirth: LocalDate? = null,

    @SerializedName("sampleCollectedAt")
    val sampleCollectedAt: Instant? = null,

    @Transient override val isProcessing: Boolean = false,
    @Transient override val lastError: Throwable? = null,

    @SerializedName("isDccSupportedByPoc")
    override val isDccSupportedByPoc: Boolean = false,
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

    val testTakenAt: Instant
        get() = sampleCollectedAt ?: testedAt

    override val isRedeemed: Boolean
        get() = testResult == PCR_OR_RAT_REDEEMED || testResult == RAT_REDEEMED

    override val isPositive: Boolean
        get() = testResult == RAT_POSITIVE

    override val isNegative: Boolean
        get() = testResult == RAT_NEGATIVE

    override val isPending: Boolean
        get() = setOf(PCR_OR_RAT_PENDING, RAT_PENDING).contains(testResult)

    override val isInvalid: Boolean
        get() = testResult == RAT_INVALID

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
