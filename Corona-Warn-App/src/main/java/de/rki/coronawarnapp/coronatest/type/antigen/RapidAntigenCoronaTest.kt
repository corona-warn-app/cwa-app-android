package de.rki.coronawarnapp.coronatest.type.antigen

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import org.joda.time.Instant
import org.joda.time.LocalDate

data class RapidAntigenCoronaTest(
    override val testGUID: CoronaTestGUID,
    override val registeredAt: Instant,
    override val registrationToken: RegistrationToken,
    override val isRefreshing: Boolean = false,
    override val isSubmitted: Boolean = false,
    override val isViewed: Boolean = false,
    override val isAdvancedConsentGiven: Boolean = false,
    override val isJournalEntryCreated: Boolean = false,
    override val isNotificationSent: Boolean = false,
    val testResult: CoronaTestResult,
    val testedAt: Instant,
    val firstName: String?,
    val lastName: String?,
    val dateOfBirth: LocalDate?,
) : CoronaTest {

    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    fun getState(nowUTC: Instant): State {
        TODO()
    }

    override val isSubmissionAllowed: Boolean = testResult == CoronaTestResult.RAT_POSITIVE

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
        OUTDATED,
    }
}
