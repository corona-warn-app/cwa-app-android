package de.rki.coronawarnapp.coronatest.type

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.reyclebin.common.Recyclable
import org.joda.time.Duration
import org.joda.time.Instant

interface CoronaTest : Recyclable {
    val identifier: TestIdentifier
    val type: Type

    val registeredAt: Instant
    val registrationToken: RegistrationToken

    val isProcessing: Boolean
    val lastError: Throwable?

    val isSubmissionAllowed: Boolean
    val isSubmitted: Boolean
    val isViewed: Boolean

    val isPositive: Boolean
    val isNegative: Boolean

    val isPending: Boolean

    val didShowBadge: Boolean

    /**
     * Has this test reached it's final state, i.e. can polling be stopped?
     */
    val isRedeemed: Boolean

    val testResultReceivedAt: Instant?
    val testResult: CoronaTestResult

    val lastUpdatedAt: Instant

    val isAdvancedConsentGiven: Boolean

    val isResultAvailableNotificationSent: Boolean

    // Is the digital green certificate supported by the point of care that issued the test
    val isDccSupportedByPoc: Boolean

    // Has the user given consent to us obtaining the DCC
    val isDccConsentGiven: Boolean

    // Has the corresponding entry been created in the test certificate storage
    val isDccDataSetCreated: Boolean

    //  The ID of the lab that uploaded the test result
    val labId: String?

    val qrCodeHash: String?

    enum class Type(val raw: String) {
        @SerializedName("PCR")
        PCR("PCR"),

        @SerializedName("RAPID_ANTIGEN")
        RAPID_ANTIGEN("RAPID_ANTIGEN"),
    }
}

typealias RegistrationToken = String
typealias TestIdentifier = String

/**
 * Tells if a [CoronaTest] age is older than the duration defined by [CoronaTestConfig]
 */
fun CoronaTest.shouldShowRiskCard(
    coronaTestConfig: CoronaTestConfig,
    now: Instant = Instant.now()
): Boolean {
    val (testTimestamp, thresholdDuration) = when (this) {
        is PCRCoronaTest -> registeredAt to coronaTestConfig.pcrParameters.durationToShowRiskCard
        is RACoronaTest -> testTakenAt to coronaTestConfig.ratParameters.durationToShowRiskCard
        else -> error("Unsupported test type=$type")
    }
    return Duration(testTimestamp, now) >= thresholdDuration
}
