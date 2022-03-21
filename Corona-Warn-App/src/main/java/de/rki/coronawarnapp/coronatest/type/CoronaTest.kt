package de.rki.coronawarnapp.coronatest.type

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import org.joda.time.Instant

interface PersonalCoronaTest : CoronaTest, KeySubmission, CoronaTestProcessorState

interface CoronaTestProcessorState {
    val isProcessing: Boolean
    val lastError: Throwable?
    val lastUpdatedAt: Instant
}

interface CoronaTestUiState {
    val isViewed: Boolean
    val didShowBadge: Boolean
    val isResultAvailableNotificationSent: Boolean
}

interface DccCoronaTest {
    // Is the digital green certificate supported by the point of care that issued the test
    val isDccSupportedByPoc: Boolean

    // Has the user given consent to us obtaining the DCC
    val isDccConsentGiven: Boolean

    // Has the corresponding entry been created in the test certificate storage
    val isDccDataSetCreated: Boolean

    val qrCodeHash: String?
}

interface KeySubmission {
    val isSubmissionAllowed: Boolean
    val isSubmitted: Boolean
    val isAdvancedConsentGiven: Boolean
}

interface CoronaTest : CoronaTestUiState, DccCoronaTest, Recyclable {

    enum class Type(val raw: String) {
        @SerializedName("PCR")
        PCR("PCR"),

        @SerializedName("RAPID_ANTIGEN")
        RAPID_ANTIGEN("RAPID_ANTIGEN"),
    }

    val identifier: TestIdentifier
    val type: Type

    val registeredAt: Instant
    val registrationToken: RegistrationToken

    val testResultReceivedAt: Instant?
    val testResult: CoronaTestResult

    /**
     * Has this test reached it's final state, i.e. can polling be stopped?
     */
    val isRedeemed: Boolean
    val isPositive: Boolean
    val isNegative: Boolean
    val isPending: Boolean

    //  The ID of the lab that uploaded the test result
    val labId: String?
}

typealias RegistrationToken = String
typealias TestIdentifier = String
