package de.rki.coronawarnapp.service.submission

import de.rki.coronawarnapp.exception.NoRegistrationTokenSetException
import de.rki.coronawarnapp.playbook.BackgroundNoise
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.SubmissionRepository
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.formatter.TestResult
import de.rki.coronawarnapp.verification.server.VerificationKeyType
import de.rki.coronawarnapp.worker.BackgroundWorkScheduler

object SubmissionService {

    private val playbook: Playbook
        get() = AppInjector.component.playbook

    private val timeStamper: TimeStamper
        get() = TimeStamper()

    suspend fun asyncRegisterDeviceViaGUID(guid: String): TestResult {
        val (registrationToken, testResult) =
            playbook.initialRegistration(
                guid,
                VerificationKeyType.GUID
            )
        LocalData.registrationToken(registrationToken)
        deleteTestGUID()
        SubmissionRepository.updateTestResult(testResult)
        LocalData.devicePairingSuccessfulTimestamp(timeStamper.nowUTC.millis)
        BackgroundNoise.getInstance().scheduleDummyPattern()
        return testResult
    }

    suspend fun asyncRegisterDeviceViaTAN(tan: String) {
        val (registrationToken, testResult) =
            playbook.initialRegistration(
                tan,
                VerificationKeyType.TELETAN
            )
        LocalData.registrationToken(registrationToken)
        deleteTeleTAN()
        SubmissionRepository.updateTestResult(testResult)
        LocalData.devicePairingSuccessfulTimestamp(timeStamper.nowUTC.millis)
        BackgroundNoise.getInstance().scheduleDummyPattern()
    }

    suspend fun asyncRequestTestResult(): TestResult {
        val registrationToken =
            LocalData.registrationToken() ?: throw NoRegistrationTokenSetException()

        return playbook.testResult(registrationToken)
    }

    fun containsValidGUID(scanResult: String): Boolean {
        val scanResult = QRScanResult(scanResult)
        return scanResult.isValid
    }

    fun storeTestGUID(guid: String) = LocalData.testGUID(guid)

    fun deleteTestGUID() {
        LocalData.testGUID(null)
    }

    fun deleteRegistrationToken() {
        LocalData.registrationToken(null)
        LocalData.devicePairingSuccessfulTimestamp(0L)
    }

    fun submissionSuccessful() {
        BackgroundWorkScheduler.stopWorkScheduler()
        LocalData.numberOfSuccessfulSubmissions(1)
    }

    private fun deleteTeleTAN() {
        LocalData.teletan(null)
    }
}
