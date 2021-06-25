package de.rki.coronawarnapp.covidcertificate.validationrules.rules

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validationrules.country.DgcCountry
import org.joda.time.DateTime
import javax.inject.Inject

class DgcRulesValidator @Inject constructor(
    testCertificateRepository: TestCertificateRepository,
    dgcDgcValidationRulesRepository: DgcValidationRulesRepository,
) {

    /**
     * Validates DGC against country of arrival's rules and issuer country rules
     */
    suspend fun validateDgc(
        arrivalsCountry: Set<DgcCountry>, // For future allow multiple country selection
        containerId: CertificateContainerId,
        dateTime: DateTime,
        cwt: CWT
    ): DgcValidationResult {
        return object : DgcValidationResult {
            override val expirationCheckPassed: Boolean
                get() = false
            override val jsonSchemaCheckPassed: Boolean
                get() = false
            override val acceptanceRulesResultDgcs: Set<DgcValidationResultSet>
                get() = setOf()
            override val invalidationRulesResultDgcs: Set<DgcValidationResultSet>
                get() = setOf()
        }
    }
}
