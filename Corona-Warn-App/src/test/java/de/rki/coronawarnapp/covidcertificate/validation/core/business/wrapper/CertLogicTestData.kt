package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule

internal val logicVaccinationDosis = ObjectMapper().readTree(
    """{"and":[{">":[{"var":"payload.v.0.dn"},0]},{">=":[{"var":"payload.v.0.dn"},{"var":"payload.v.0.sd"}]}]}"""
)

internal val logicVersion = ObjectMapper().readTree(
    """{"and":[{"EQ":[{"var":"payload.ver"},"1.0.0"]}]}"""
)

internal fun createVaccinationRule(
    validFrom: String,
    validTo: String,
) = DccValidationRule(
    identifier = "VR-DE-1",
    version = "1.0.0",
    schemaVersion = "1.0.0",
    engine = "CERTLOGIC",
    engineVersion = "1.0.0",
    typeDcc = DccValidationRule.Type.ACCEPTANCE,
    country = "DE",
    certificateType = VACCINATION,
    description = mapOf("en" to "Vaccination must be complete"),
    validFrom = validFrom,
    validTo = validTo,
    affectedFields = listOf("v.0.dn", "v.0.sd"),
    logic = logicVaccinationDosis
)

internal fun createGeneralRule(
    validFrom: String = "2021-05-27T07:46:40Z",
    validTo: String = "2022-08-01T07:46:40Z",
) = DccValidationRule(
    identifier = "GE-DE-1",
    version = "1.0.0",
    schemaVersion = "1.0.0",
    engine = "CERTLOGIC",
    engineVersion = "1.0.0",
    typeDcc = DccValidationRule.Type.ACCEPTANCE,
    country = "DE",
    certificateType = GENERAL,
    description = mapOf("en" to "Version must be 1.0.0"),
    validFrom = validFrom,
    validTo = validTo,
    affectedFields = listOf("payload.ver"),
    logic = logicVersion
)
