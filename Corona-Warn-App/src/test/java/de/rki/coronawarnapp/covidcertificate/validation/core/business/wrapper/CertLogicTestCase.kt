package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.fasterxml.jackson.annotation.JsonProperty
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule

data class CertLogicTestCases(
    @JsonProperty("general")
    val general: General,

    @JsonProperty("testCases")
    val testCases: List<CertLogicTestCase>
)

data class General(
    @JsonProperty("valueSetProtocolBuffer")
    val valueSetProtocolBuffer: String
)

data class CertLogicTestCase(
    @JsonProperty("description")
    val description: String,

    @JsonProperty("dcc")
    val dcc: String,

    @JsonProperty("rules")
    val rules: List<DccValidationRule>,

    @JsonProperty("countryOfArrival")
    val countryOfArrival: String,

    @JsonProperty("validationClock")
    val validationClock: String,

    @JsonProperty("expPass")
    val expPass: Int,

    @JsonProperty("expFail")
    val expFail: Int,

    @JsonProperty("expOpen")
    val expOpen: Int
)
