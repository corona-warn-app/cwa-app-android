package de.rki.coronawarnapp.coronatest.type.rapidantigen

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import org.joda.time.Duration
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

    @SerializedName("outdatedAfter")
    val outdatedAfter: Duration,

    @Transient override val isProcessing: Boolean = false,
    @Transient override val lastError: Throwable? = null,
) : CoronaTest {

    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    private fun isOutdated(nowUTC: Instant) = testedAt.plus(outdatedAfter).isBefore(nowUTC)

    fun getState(nowUTC: Instant) =
        if (isOutdated(nowUTC)) {
            State.OUTDATED
        } else {
            when (testResult) {
                CoronaTestResult.PCR_OR_RAT_PENDING -> State.PENDING
                CoronaTestResult.RAT_NEGATIVE -> State.NEGATIVE
                CoronaTestResult.RAT_POSITIVE -> State.POSITIVE
                CoronaTestResult.RAT_INVALID -> State.INVALID
                CoronaTestResult.RAT_REDEEMED -> State.REDEEMED
                else -> throw IllegalArgumentException("Invalid RAT test state $testResult")
            }
        }

    override val isPositive: Boolean = testResult == CoronaTestResult.RAT_POSITIVE

    override val isSubmissionAllowed: Boolean = isPositive && !isSubmitted

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
        OUTDATED,
    }
}
