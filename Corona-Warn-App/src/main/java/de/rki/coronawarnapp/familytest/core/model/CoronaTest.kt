package de.rki.coronawarnapp.familytest.core.model

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestDcc
import de.rki.coronawarnapp.coronatest.type.CoronaTestUiState
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import java.time.Instant
import java.time.LocalDate

data class CoronaTest(
    @SerializedName("identifier")
    override val identifier: TestIdentifier,

    @SerializedName("type")
    override val type: BaseCoronaTest.Type,

    @SerializedName("registeredAt")
    override val registeredAt: Instant,

    @SerializedName("registrationToken")
    override val registrationToken: RegistrationToken,

    @SerializedName("testResult")
    override val testResult: CoronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING,

    @SerializedName("labId")
    override val labId: String? = null,

    @SerializedName("qrCodeHash")
    override val qrCodeHash: String? = null,

    @SerializedName("dcc")
    val dcc: Dcc = Dcc(),

    @SerializedName("uiState")
    val uiState: UiState = UiState(),

    @SerializedName("additionalInfo")
    val additionalInfo: AdditionalInfo? = null,

    @Transient
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
        registeredAt.toUserTimeZone().toLocalDate().toShortDayFormat()

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

    override val isRedeemed: Boolean
        get() = state == State.REDEEMED

    override val isPositive: Boolean
        get() = state == State.POSITIVE

    override val isNegative: Boolean
        get() = state == State.NEGATIVE

    override val isPending: Boolean
        get() = state == State.PENDING

    override val isInvalid: Boolean
        get() = state == State.INVALID

    data class Dcc(
        @SerializedName("isDccSupportedByPoc")
        override val isDccSupportedByPoc: Boolean = true,

        @SerializedName("isDccConsentGiven")
        override val isDccConsentGiven: Boolean = false,

        @SerializedName("isDccDataSetCreated")
        override val isDccDataSetCreated: Boolean = false,
    ) : CoronaTestDcc

    data class UiState(
        @SerializedName("isViewed")
        override val isViewed: Boolean = false,

        @SerializedName("didShowBadge")
        override val didShowBadge: Boolean = false,

        @SerializedName("isResultAvailableNotificationSent")
        override val isResultAvailableNotificationSent: Boolean = false,

        @SerializedName("hasResultChangeBadge")
        override val hasResultChangeBadge: Boolean = false,

    ) : CoronaTestUiState

    data class AdditionalInfo(
        @SerializedName("createdAt")
        val createdAt: Instant,

        @SerializedName("firstName")
        val firstName: String? = null,

        @SerializedName("lastName")
        val lastName: String? = null,

        @SerializedName("dateOfBirth")
        val dateOfBirth: LocalDate? = null,

        @SerializedName("sampleCollectedAt")
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
    val additionalInfo = additionalInfo?.copy(sampleCollectedAt = sampleCollectedAt)
        // shouldn't occur, sampleCollectedAt should also be when the test has been created
        ?: CoronaTest.AdditionalInfo(createdAt = sampleCollectedAt, sampleCollectedAt = sampleCollectedAt)
    return copy(additionalInfo = additionalInfo)
}
