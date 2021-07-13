package de.rki.coronawarnapp.covidcertificate.validation.core.rule

import de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper.createDccRule
import dgca.verifier.app.engine.data.CertificateType
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccValidationRuleTest : BaseTest() {

    @Test
    fun `version comparison`() {
        val rules = listOf(
            createDccRule(identifier = "R-1", version = "1.0.0", certificateType = CertificateType.GENERAL),
            createDccRule(identifier = "R-2", version = "1.2.0", certificateType = CertificateType.GENERAL),
            createDccRule(identifier = "R-3", version = "1.3.0", certificateType = CertificateType.GENERAL),
            createDccRule(identifier = "R-2", version = "1.1.0", certificateType = CertificateType.GENERAL),
            createDccRule(identifier = "R-2", version = "1.0.42", certificateType = CertificateType.GENERAL),
            createDccRule(identifier = "R-3", version = "1.4.0", certificateType = CertificateType.GENERAL),
        )

        rules
            .groupBy { it.identifier }
            .map { entry ->
                entry.value.maxByOrNull { it.versionSemVer }
            } shouldBe listOf(
            createDccRule(identifier = "R-1", version = "1.0.0", certificateType = CertificateType.GENERAL),
            createDccRule(identifier = "R-2", version = "1.2.0", certificateType = CertificateType.GENERAL),
            createDccRule(identifier = "R-3", version = "1.4.0", certificateType = CertificateType.GENERAL),
        )
    }
}
