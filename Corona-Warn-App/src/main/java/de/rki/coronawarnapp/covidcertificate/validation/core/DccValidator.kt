package de.rki.coronawarnapp.covidcertificate.validation.core

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchemaValidator
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureValidator
import de.rki.coronawarnapp.covidcertificate.validation.core.business.BusinessValidator
import de.rki.coronawarnapp.util.TimeStamper
import de.rki.coronawarnapp.util.toLocalDateTimeUtc
import timber.log.Timber
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class DccValidator @Inject constructor(
    private val businessValidator: BusinessValidator,
    private val dccJsonSchemaValidator: DccJsonSchemaValidator,
    private val dscSignatureValidator: DscSignatureValidator,
    private val timeStamper: TimeStamper,
) {

    /**
     * Validates DCC against the rules of the country of arrival and issuer country
     */
    suspend fun validateDcc(
        userInput: ValidationUserInput,
        certificate: DccData<out DccV1.MetaData>,
    ): DccValidation {
        Timber.tag(TAG).v("validateDcc(country=%s)", userInput.arrivalCountry)

        val signatureCheckPassed = isSignatureValid(
            certificate,
            Date.from(
                userInput.arrivalDateTime.atZone(ZoneId.systemDefault()).toInstant()
            )
        )
        val expirationCheckPassed = certificate.expiresAfter(userInput.arrivalDateTime)
        val jsonSchemaCheckPassed = dccJsonSchemaValidator.isValid(certificate.certificateJson).isValid

        val businessValidation = businessValidator.validate(
            userInput.arrivalCountry,
            userInput.arrivalDateTime,
            certificate
        )

        return DccValidation(
            userInput = userInput,
            validatedAt = timeStamper.nowUTC,
            signatureCheckPassed = signatureCheckPassed,
            expirationCheckPassed = expirationCheckPassed,
            jsonSchemaCheckPassed = jsonSchemaCheckPassed,
            acceptanceRules = businessValidation.acceptanceRules,
            invalidationRules = businessValidation.invalidationRules
        )
    }

    private suspend fun isSignatureValid(dccData: DccData<out DccV1.MetaData>, date: Date): Boolean = try {
        dscSignatureValidator.validateSignature(dccData, date = date)
        true
    } catch (e: Exception) {
        Timber.tag(TAG).d(e)
        false
    }

    companion object {
        private const val TAG = "DccValidator"
    }
}

@VisibleForTesting
internal fun DccData<*>.expiresAfter(referenceDate: LocalDateTime): Boolean {
    return header.expiresAt.toLocalDateTimeUtc() > referenceDate
}
