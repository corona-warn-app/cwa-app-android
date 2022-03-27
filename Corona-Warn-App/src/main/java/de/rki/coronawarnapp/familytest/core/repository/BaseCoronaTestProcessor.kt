package de.rki.coronawarnapp.familytest.core.repository

import dagger.Reusable
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult.PCR_OR_RAT_REDEEMED
import de.rki.coronawarnapp.coronatest.server.CoronaTestResultResponse
import de.rki.coronawarnapp.coronatest.server.RegistrationRequest
import de.rki.coronawarnapp.coronatest.server.VerificationKeyType
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.PCR
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest.Type.RAPID_ANTIGEN
import de.rki.coronawarnapp.coronatest.type.CoronaTestService
import de.rki.coronawarnapp.coronatest.type.common.DateOfBirthKey
import de.rki.coronawarnapp.coronatest.type.isOlderThan21Days
import de.rki.coronawarnapp.coronatest.type.pcr.toValidatedPcrResult
import de.rki.coronawarnapp.coronatest.type.rapidantigen.toValidatedRaResult
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.http.BadRequestException
import de.rki.coronawarnapp.exception.http.CwaWebException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class BaseCoronaTestProcessor @Inject constructor(
    private val timeStamper: TimeStamper,
    private val coronaTestService: CoronaTestService,
) {

    suspend fun register(qrCode: CoronaTestQRCode): CoronaTest {

        val serverRequest = createServerRequest(qrCode)

        val registrationData = coronaTestService.registerTest(serverRequest)

        val testResult = when (qrCode.type) {
            PCR -> registrationData.testResultResponse.coronaTestResult.toValidatedPcrResult()
            RAPID_ANTIGEN -> registrationData.testResultResponse.coronaTestResult.toValidatedRaResult()
        }

        val additionalInfo = if (qrCode is CoronaTestQRCode.RapidAntigen) CoronaTest.AdditionalInfo(
            firstName = qrCode.firstName,
            lastName = qrCode.lastName,
            dateOfBirth = qrCode.dateOfBirth,
            sampleCollectedAt = registrationData.testResultResponse.sampleCollectedAt,
            createdAt = qrCode.createdAt,
        ) else null

        return CoronaTest(
            type = qrCode.type,
            identifier = qrCode.identifier,
            registeredAt = timeStamper.nowUTC,
            registrationToken = registrationData.registrationToken,
            testResult = testResult,
            qrCodeHash = qrCode.rawQrCode.toSHA256(),
            dcc = CoronaTest.Dcc(
                isDccSupportedByPoc = qrCode.isDccSupportedByPoc,
                isDccConsentGiven = qrCode.isDccConsentGiven,
            ),
            additionalInfo = additionalInfo,
            labId = registrationData.testResultResponse.labId,
        )
    }

    suspend fun pollServer(test: CoronaTest): CoronaTestResultUpdate? {

        return try {
            val response = try {
                coronaTestService.checkTestResult(test.registrationToken)
            } catch (e: BadRequestException) {
                if (test.isOlderThan21Days(timeStamper.nowUTC)) {
                    Timber.v("HTTP 400 error after 21 days, remapping to PCR_OR_RAT_REDEEMED.")
                    CoronaTestResultResponse(coronaTestResult = PCR_OR_RAT_REDEEMED)
                } else {
                    throw e
                }
            }

            val testResult = when (test.type) {
                RAPID_ANTIGEN -> response.coronaTestResult.toValidatedRaResult()
                PCR -> response.coronaTestResult.toValidatedPcrResult()
            }

            CoronaTestResultUpdate(
                coronaTestResult = testResult,
                sampleCollectedAt = response.sampleCollectedAt,
                labId = response.labId
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to poll server for  %s", test)
            if (e !is CwaWebException) e.report(ExceptionCategory.INTERNAL)
            null
        }
    }

    private fun createServerRequest(qrCode: CoronaTestQRCode): RegistrationRequest {
        val dateOfBirthKey = if (qrCode.isDccConsentGiven && qrCode.dateOfBirth != null) {
            DateOfBirthKey(qrCode.registrationIdentifier, qrCode.dateOfBirth!!)
        } else null

        return RegistrationRequest(
            key = qrCode.registrationIdentifier,
            dateOfBirthKey = dateOfBirthKey,
            type = VerificationKeyType.GUID,
        )
    }

    data class CoronaTestResultUpdate(
        val coronaTestResult: CoronaTestResult,
        val sampleCollectedAt: Instant? = null,
        val labId: String? = null,
    )
}
