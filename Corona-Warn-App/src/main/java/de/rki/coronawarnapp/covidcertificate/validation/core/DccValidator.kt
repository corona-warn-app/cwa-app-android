package de.rki.coronawarnapp.covidcertificate.validation.core

import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchemaValidator
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.validation.business.BusinessValidator
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

class DccValidator @Inject constructor(
    private val businessValidator: BusinessValidator,
    private val dccJsonSchemaValidator: DccJsonSchemaValidator
) {

    /**
     * Validates DCC against the rules of the country of arrival and issuer country
     */
    suspend fun validateDcc(
        arrivalCountry: DccCountry,
        arrivalTime: Instant,
        certificate: DccData<out DccV1.MetaData>,
    ): DccValidation {
        Timber.tag(TAG).v("validateDcc(country=%s)", arrivalCountry)

        // TODO update value sets, acceptance rules, and invalidation rules

        val expirationCheckPassed = certificate.expiresAfter(arrivalTime)
        val jsonSchemaCheckPassed = dccJsonSchemaValidator.isValid(certificate.certificateJson).isValid

        val businessValidation = businessValidator.validate(
            arrivalCountry,
            arrivalTime,
            certificate
        )

        return DccValidation(
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

private fun DccData<*>.expiresAfter(referenceDate: Instant): Boolean {
    return header.expiresAt.millis >= referenceDate.millis
}
