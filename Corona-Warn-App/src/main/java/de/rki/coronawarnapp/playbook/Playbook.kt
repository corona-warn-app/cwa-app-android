package de.rki.coronawarnapp.playbook

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass

/**
 * The concept of Plausible Deniability aims to hide the existence of a positive test result by always using a defined
 * “playbook pattern” of requests to the Verification Server and CWA Backend so it is impossible for an attacker to
 * identify which communication was done.
 *
 * The “playbook pattern” represents a well-defined communication pattern consisting of fake requests and real
 * requests.
 *
 * To hide that a real request was done, the device does multiple of these requests over a longer period of time
 * according to the previously defined communication pattern statistically similar to all apps so it is not possible to
 * infer by observing the traffic if the requests under concern are real or the fake ones.
 */
interface Playbook {

    /**
     * @return pair of Registration token [String] & [CoronaTestResult]
     */
    suspend fun initialRegistration(
        key: String,
        keyType: VerificationKeyType
    ): Pair<String, CoronaTestResult>

    suspend fun testResult(registrationToken: String): CoronaTestResult

    suspend fun submit(data: SubmissionData)

    suspend fun dummy()

    data class SubmissionData(
        val registrationToken: String,
        val temporaryExposureKeys: List<TemporaryExposureKeyExportOuterClass.TemporaryExposureKey>,
        val consentToFederation: Boolean,
        val visitedCountries: List<String>,
        val checkIns: List<CheckInOuterClass.CheckIn>
    )
}
