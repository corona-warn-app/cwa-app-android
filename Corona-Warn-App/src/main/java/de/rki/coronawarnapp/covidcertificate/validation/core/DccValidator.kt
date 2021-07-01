package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.business.BusinessValidator
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

class DccValidator @Inject constructor(
    private val businessValidator: BusinessValidator,
) {

    /**
     * Validates DCC against the rules of the country of arrival and issuer country
     */
    suspend fun validateDcc(
        arrivalCountry: DccCountry,
        arrivalTime: Instant,
        certificate: DccData<*>,
    ): DccValidation {
        Timber.tag(TAG).v("validateDcc(country=%s)", arrivalCountry)

        val businessValidation = businessValidator.validate(
            arrivalCountry,
            arrivalTime,
            certificate
        )

        return DccValidation(
            expirationCheckPassed = certificate.expiresAfter(arrivalTime),
            jsonSchemaCheckPassed = true, // certificate is already checked while decoding
            acceptanceRules = businessValidation.acceptanceRules,
            invalidationRules = businessValidation.invalidationRules
        )
    }

    companion object {
        private const val TAG = "DccValidator"
    }
}

fun DccData<*>.expiresAfter(referenceDate: Instant): Boolean {
    return header.expiresAt.millis >= referenceDate.millis
}
