package de.rki.coronawarnapp.covidcertificate.validation.core

import androidx.annotation.VisibleForTesting
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchemaValidator
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.business.BusinessValidator
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTime
import de.rki.coronawarnapp.util.TimeStamper
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import timber.log.Timber
import javax.inject.Inject

class DccValidator @Inject constructor(
    private val businessValidator: BusinessValidator,
    private val dccJsonSchemaValidator: DccJsonSchemaValidator,
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
            expirationCheckPassed = expirationCheckPassed,
            jsonSchemaCheckPassed = jsonSchemaCheckPassed,
            acceptanceRules = businessValidation.acceptanceRules,
            invalidationRules = businessValidation.invalidationRules
        )
    }

    companion object {
        private const val TAG = "DccValidator"
    }
}

@VisibleForTesting
internal fun DccData<*>.expiresAfter(referenceDate: LocalDateTime): Boolean {
    return header.expiresAt.toLocalDateTime(DateTimeZone.UTC) > referenceDate
}
