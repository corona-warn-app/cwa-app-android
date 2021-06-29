package de.rki.coronawarnapp.covidcertificate.validation.core.validation.technical

import dagger.Reusable
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import org.joda.time.Instant
import javax.inject.Inject

@Reusable
class TechnicalValidator @Inject constructor() {

    suspend fun validate(
        validationClock: Instant,
        certificate: DccData<*>,
    ): TechnicalValidation = object : TechnicalValidation {
        // TODO
        override val expirationCheckPassed: Boolean
            get() = true
        override val jsonSchemaCheckPassed: Boolean
            get() = true
    }
}
