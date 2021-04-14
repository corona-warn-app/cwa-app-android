package de.rki.coronawarnapp.coronatest.type.rapidantigen

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import org.joda.time.Instant
import org.joda.time.LocalDate

data class RapidAntigenCoronaTest(
    @SerializedName("testGUID") override val testGUID: CoronaTestGUID,
    @SerializedName("registeredAt") override val registeredAt: Instant,
    @SerializedName("registrationToken") override val registrationToken: RegistrationToken,
    @SerializedName("isSubmitted") override val isSubmitted: Boolean = false,
    @SerializedName("isViewed") override val isViewed: Boolean = false,
    @SerializedName("isAdvancedConsentGiven") override val isAdvancedConsentGiven: Boolean = false,
    @SerializedName("isJournalEntryCreated") override val isJournalEntryCreated: Boolean = false,
    @SerializedName("isNotificationSent") override val isNotificationSent: Boolean = false,
    @SerializedName("testResult") val testResult: CoronaTestResult,
    @SerializedName("testedAt") val testedAt: Instant,
    @SerializedName("firstName") val firstName: String?,
    @SerializedName("lastName") val lastName: String?,
    @SerializedName("dateOfBirth") val dateOfBirth: LocalDate?,
    @Transient override val isProcessing: Boolean = false,
    @Transient override val lastError: Throwable? = null,
) : CoronaTest {

    override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

    fun getState(nowUTC: Instant): State {
        // TODO
        return State.PENDING
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
