package de.rki.coronawarnapp.coronatest.type

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import org.joda.time.Instant

// test belonging to the main user of the app
interface PersonalCoronaTest : CoronaTest, CoronaTestKeySubmission, CoronaTestProcessorState, CoronaTestLegacy

interface CoronaTest : CoronaTestUiState, CoronaTestDcc, Recyclable {

    enum class Type(val raw: String) {
        @SerializedName("PCR")
        PCR("PCR"),

        @SerializedName("RAPID_ANTIGEN")
        RAPID_ANTIGEN("RAPID_ANTIGEN"),
    }

    val type: Type
    val identifier: TestIdentifier
    val qrCodeHash: String?

    val registrationToken: RegistrationToken
    val registeredAt: Instant

    val testResult: CoronaTestResult

    val isRedeemed: Boolean
    val isPositive: Boolean
    val isNegative: Boolean
    val isPending: Boolean

    //  The ID of the lab that uploaded the test result
    val labId: String?
}

interface CoronaTestUiState {
    // final test result has been seen by the user
    val isViewed: Boolean

    // badge for final test result
    val didShowBadge: Boolean

    // notification for final test result
    val isResultAvailableNotificationSent: Boolean
}

interface CoronaTestDcc {
    // Is the DCC supported by the point of care that issued the test
    val isDccSupportedByPoc: Boolean

    // Has the user given consent to obtaining the DCC
    val isDccConsentGiven: Boolean

    // Has the corresponding entry been created in the test certificate storage
    val isDccDataSetCreated: Boolean
}

interface CoronaTestKeySubmission {
    val isSubmissionAllowed: Boolean
    val isSubmitted: Boolean
    val isAdvancedConsentGiven: Boolean
}

interface CoronaTestProcessorState {
    val isProcessing: Boolean
    val lastError: Throwable?
    val lastUpdatedAt: Instant
}

interface CoronaTestLegacy {
    val testResultReceivedAt: Instant?
}

typealias RegistrationToken = String
typealias TestIdentifier = String
