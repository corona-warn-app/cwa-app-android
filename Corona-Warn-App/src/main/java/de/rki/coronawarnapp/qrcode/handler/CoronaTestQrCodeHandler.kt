package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.qrcode.InvalidQRCodeException
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents.NavigateToDuplicateWarningFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents.NavigateToRequestDccFragment
import de.rki.coronawarnapp.ui.submission.viewmodel.SubmissionNavigationEvents.RegisterTestResult
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CoronaTestQrCodeHandler @Inject constructor(
    private val registrationProcessor: TestRegistrationStateProcessor,
    private val submissionRepository: SubmissionRepository
) {

    /**
     * @throws [InvalidQRCodeException]
     */
    suspend fun handleQrCode(qrcode: CoronaTestQRCode): SubmissionNavigationEvents {
        val coronaTest = submissionRepository.testForType(qrcode.type).first()
        return when {
            coronaTest != null -> NavigateToDuplicateWarningFragment(qrcode, consentGiven = false)
            !qrcode.isDccSupportedByPoc -> RegisterTestResult(registrationProcessor.registerCoronaTest(qrcode))
            else -> NavigateToRequestDccFragment(qrcode, consentGiven = false)
        }
    }
}
