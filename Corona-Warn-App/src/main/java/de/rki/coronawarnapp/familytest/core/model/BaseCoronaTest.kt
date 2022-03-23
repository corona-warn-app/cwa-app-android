package de.rki.coronawarnapp.familytest.core.model

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.CoronaTestDcc
import de.rki.coronawarnapp.coronatest.type.CoronaTestUiState
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import org.joda.time.Instant
import org.joda.time.LocalDate

data class BaseCoronaTest(
    @SerializedName("type")
    override val type: CoronaTest.Type,

    @SerializedName("identifier")
    override val identifier: TestIdentifier,

    @SerializedName("registeredAt")
    override val registeredAt: Instant,

    @SerializedName("registrationToken")
    override val registrationToken: RegistrationToken,

    @SerializedName("testResultReceivedAt")
    override val testResultReceivedAt: Instant? = null,

    @SerializedName("testResult")
    override val testResult: CoronaTestResult = CoronaTestResult.PCR_OR_RAT_PENDING,

    @SerializedName("labId")
    override val labId: String? = null,

    @SerializedName("qrCodeHash")
    override val qrCodeHash : String? = null,

    @SerializedName("recycledAt")
    override var recycledAt: Instant? = null,

    val dcc: Dcc = Dcc(),
    val uiState: UiState = UiState(),
    val additionalInfo: AdditionalInfo? = null
):
    CoronaTest,
    CoronaTestUiState by uiState,
    CoronaTestDcc by dcc {

    val state: FamilyCoronaTest.State
        get() = when {
            isRecycled -> FamilyCoronaTest.State.RECYCLED
            else -> when (testResult) {
                CoronaTestResult.PCR_OR_RAT_PENDING,
                CoronaTestResult.RAT_PENDING, -> FamilyCoronaTest.State.PENDING

                CoronaTestResult.RAT_NEGATIVE,
                CoronaTestResult.PCR_NEGATIVE -> FamilyCoronaTest.State.NEGATIVE

                CoronaTestResult.RAT_POSITIVE,
                CoronaTestResult.PCR_POSITIVE -> FamilyCoronaTest.State.POSITIVE

                CoronaTestResult.RAT_INVALID,
                CoronaTestResult.PCR_INVALID -> FamilyCoronaTest.State.INVALID

                CoronaTestResult.PCR_OR_RAT_REDEEMED,
                CoronaTestResult.RAT_REDEEMED -> FamilyCoronaTest.State.REDEEMED
            }
        }

    override val isRedeemed: Boolean
        get() = state == FamilyCoronaTest.State.REDEEMED

    override val isPositive: Boolean
        get() = state == FamilyCoronaTest.State.POSITIVE

    override val isNegative: Boolean
        get() = state == FamilyCoronaTest.State.NEGATIVE

    override val isPending: Boolean
        get() = state == FamilyCoronaTest.State.PENDING

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
    ): CoronaTestUiState

    data class AdditionalInfo(
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
    )
}


