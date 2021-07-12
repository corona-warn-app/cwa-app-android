package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule

data class CertLogicTestCases(
    val general: General,
    val testCases: List<CertLogicTestCase>
)

data class General(
    val valueSetProtocolBuffer: String
)

data class CertLogicTestCase(
    val description: String,
    val dcc: String,
    val rules: List<DccValidationRule>,
    val countryOfArrival: String,
    val validationClock: String,
    val expPass: Int,
    val expFail: Int,
    val expOpen: Int
)

