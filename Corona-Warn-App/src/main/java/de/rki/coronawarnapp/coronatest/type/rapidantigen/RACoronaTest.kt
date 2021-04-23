package de.rki.coronawarnapp.coronatest.type.rapidantigen

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_INVALID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_NEGATIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_PENDING
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_POSITIVE
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.type.CoronaTest
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

    @SerializedName("testedAt")
    val testedAt: Instant,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("dateOfBirth")
    val dateOfBirth: LocalDate?,

    @Transient override val isProcessing: Boolean = false,
    @Transient override val lastError: Throwable? = null,
) : CoronaTest {

    override val type: CoronaTest.Type
        get() = CoronaTest.Type.RAPID_ANTIGEN

    private fun isOutdated(nowUTC: Instant, testConfig: CoronaTestConfig) =
        testedAt.plus(testConfig.coronaRapidAntigenTestParameters.hoursToDeemTestOutdated).isBefore(nowUTC)

    fun getState(nowUTC: Instant, testConfig: CoronaTestConfig) =
        if (testResult == RAT_NEGATIVE && isOutdated(nowUTC, testConfig)) {
            State.OUTDATED
        } else {
            when (testResult) {
                PCR_OR_RAT_PENDING,
                RAT_PENDING -> State.PENDING
                RAT_NEGATIVE -> State.NEGATIVE
                RAT_POSITIVE -> State.POSITIVE
                RAT_INVALID -> State.INVALID
                RAT_REDEEMED -> State.REDEEMED
                else -> throw IllegalArgumentException("Invalid RAT test state $testResult")
            }
        }

    override val isPositive: Boolean
        get() = testResult == RAT_POSITIVE

    override val isPending: Boolean
        get() = setOf(PCR_OR_RAT_PENDING, RAT_PENDING).contains(testResult)

    override val isSubmissionAllowed: Boolean
        get() = isPositive && !isSubmitted

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
        OUTDATED,
    }
}
