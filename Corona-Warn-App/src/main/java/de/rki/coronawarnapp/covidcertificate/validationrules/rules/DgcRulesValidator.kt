package de.rki.coronawarnapp.covidcertificate.validationrules.rules

import de.rki.coronawarnapp.covidcertificate.common.repository.CertificateContainerId
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificateRepository
import de.rki.coronawarnapp.covidcertificate.validationrules.country.DgcCountry
import org.joda.time.DateTime
import javax.inject.Inject

class DgcRulesValidator @Inject constructor(
    testCertificateRepository: TestCertificateRepository,
    dgcValidationRulesRepository: ValidationRulesRepository,
) {

    /**
     * Validates DGC against country of arrival's rules and issuer country rules
     */
    suspend fun validateDgc(
        arrivalsCountry: Set<DgcCountry>, // For future
        containerId: CertificateContainerId,
        dateTime: DateTime
    ): DgcValidationResult {

        return DgcValidationResult.FAIL
    }
}
