package de.rki.coronawarnapp.familytest.core.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestDcc
import de.rki.coronawarnapp.coronatest.type.CoronaTestUiState
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class CoronaTest(
    @JsonProperty("identifier")
    override val identifier: TestIdentifier,

    @JsonProperty("type")
    override val type: BaseCoronaTest.Type,

    @JsonProperty("registeredAt")
    override val registeredAt: Instant,

    @JsonProperty("registrationToken")
    override val registrationToken: RegistrationToken,

    @JsonProperty("testResult")
    override val testResult: CoronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING,

    @JsonProperty("labId")
    override val labId: String? = null,

    @JsonProperty("qrCodeHash")
    override val qrCodeHash: String? = null,

    @JsonProperty("dcc")
    val dcc: Dcc = Dcc(),

    @JsonProperty("uiState")
    val uiState: UiState = UiState(),

    @JsonProperty("additionalInfo")
    val additionalInfo: AdditionalInfo? = null,

    @JsonIgnore
    override var recycledAt: Instant? = null,
) :
    BaseCoronaTest,
    CoronaTestUiState by uiState,
    CoronaTestDcc by dcc {

    enum class State {
        PENDING,
        INVALID,
        POSITIVE,
        NEGATIVE,
        REDEEMED,
        OUTDATED,
        RECYCLED,
    }

    fun getFormattedRegistrationDate(): String =
        registeredAt.toLocalDateTimeUserTz().toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
    @get:JsonIgnore
    val testTakenAt: Instant
        get() = (additionalInfo?.sampleCollectedAt ?: additionalInfo?.createdAt) as Instant

    private fun isOutdated(nowUTC: Instant, testConfig: CoronaTestConfig): Boolean =
        testTakenAt.plus(testConfig.ratParameters.hoursToDeemTestOutdated).isBefore(nowUTC)

    fun getUiState(nowUTC: Instant, testConfig: CoronaTestConfig) = when {
        isRecycled -> State.RECYCLED
        testResult == CoronaTestResult.RAT_NEGATIVE && isOutdated(nowUTC, testConfig) -> State.OUTDATED
        else -> when (testResult) {
            CoronaTestResult.PCR_OR_RAT_PENDING,
            CoronaTestResult.RAT_PENDING,
            -> State.PENDING

            CoronaTestResult.RAT_NEGATIVE,
            CoronaTestResult.PCR_NEGATIVE -> State.NEGATIVE

            CoronaTestResult.RAT_POSITIVE,
            CoronaTestResult.PCR_POSITIVE -> State.POSITIVE

            CoronaTestResult.RAT_INVALID,
            CoronaTestResult.PCR_INVALID -> State.INVALID

            CoronaTestResult.PCR_OR_RAT_REDEEMED,
            CoronaTestResult.RAT_REDEEMED -> State.REDEEMED
        }
    }
    @get:JsonIgnore
    val state: State
        get() = when {
            isRecycled -> State.RECYCLED
            else -> when (testResult) {
                CoronaTestResult.PCR_OR_RAT_PENDING,
                CoronaTestResult.RAT_PENDING -> State.PENDING

                CoronaTestResult.RAT_NEGATIVE,
                CoronaTestResult.PCR_NEGATIVE -> State.NEGATIVE

                CoronaTestResult.RAT_POSITIVE,
                CoronaTestResult.PCR_POSITIVE -> State.POSITIVE

                CoronaTestResult.RAT_INVALID,
                CoronaTestResult.PCR_INVALID -> State.INVALID

                CoronaTestResult.PCR_OR_RAT_REDEEMED,
                CoronaTestResult.RAT_REDEEMED -> State.REDEEMED
            }
        }
    @get:JsonIgnore
    override val isRedeemed: Boolean
        get() = state == State.REDEEMED
    @get:JsonIgnore
    override val isPositive: Boolean
        get() = state == State.POSITIVE
    @get:JsonIgnore
    override val isNegative: Boolean
        get() = state == State.NEGATIVE
    @get:JsonIgnore
    override val isPending: Boolean
        get() = state == State.PENDING
    @get:JsonIgnore
    override val isInvalid: Boolean
        get() = state == State.INVALID

    data class Dcc(
        @JsonProperty("isDccSupportedByPoc")
        override val isDccSupportedByPoc: Boolean = true,

        @JsonProperty("isDccConsentGiven")
        override val isDccConsentGiven: Boolean = false,

        @JsonProperty("isDccDataSetCreated")
        override val isDccDataSetCreated: Boolean = false,
    ) : CoronaTestDcc

    data class UiState(
        @JsonProperty("isViewed")
        override val isViewed: Boolean = false,

        @JsonProperty("didShowBadge")
        override val didShowBadge: Boolean = false,

        @JsonProperty("isResultAvailableNotificationSent")
        override val isResultAvailableNotificationSent: Boolean = false,

        @JsonProperty("hasResultChangeBadge")
        override val hasResultChangeBadge: Boolean = false,
    ) : CoronaTestUiState

    data class AdditionalInfo(
        @JsonProperty("createdAt")
        val createdAt: Instant,

        @JsonProperty("firstName")
        val firstName: String? = null,

        @JsonProperty("lastName")
        val lastName: String? = null,

        @JsonProperty("dateOfBirth")
        val dateOfBirth: LocalDate? = null,

        @JsonProperty("sampleCollectedAt")
        val sampleCollectedAt: Instant? = null,
    )
}

internal fun CoronaTest.markViewed(): CoronaTest {
    return copy(uiState = uiState.copy(isViewed = true))
}

internal fun CoronaTest.markBadgeAsViewed(): CoronaTest {
    return copy(uiState = uiState.copy(didShowBadge = true, hasResultChangeBadge = false))
}

internal fun CoronaTest.updateResultNotification(sent: Boolean): CoronaTest {
    return copy(uiState = uiState.copy(isResultAvailableNotificationSent = sent))
}

internal fun CoronaTest.restore(): CoronaTest {
    return copy(recycledAt = null)
}

internal fun CoronaTest.moveToRecycleBin(now: Instant): CoronaTest {
    return copy(recycledAt = now)
}

internal fun CoronaTest.updateTestResult(testResult: CoronaTestResult): CoronaTest {
    return copy(testResult = testResult)
}

internal fun CoronaTest.updateLabId(labId: String): CoronaTest {
    return copy(labId = labId)
}

internal fun CoronaTest.updateSampleCollectedAt(sampleCollectedAt: Instant): CoronaTest {
    // shouldn't occur, sampleCollectedAt should also be when the test has been created
    val additionalInfo = additionalInfo?.copy(sampleCollectedAt = sampleCollectedAt)
        ?: CoronaTest.AdditionalInfo(createdAt = sampleCollectedAt, sampleCollectedAt = sampleCollectedAt)
    return copy(additionalInfo = additionalInfo)
}
