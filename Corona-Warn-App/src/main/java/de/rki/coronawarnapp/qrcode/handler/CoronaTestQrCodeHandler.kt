package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.TestRegistrationStateProcessor
import javax.inject.Inject

class CoronaTestQrCodeHandler @Inject constructor(
    private val registrationStateProcessor: TestRegistrationStateProcessor,
    private val submissionRepository: SubmissionRepository
) {

    fun handleQrCode(qrcode: CoronaTestQRCode) {
    }
}
