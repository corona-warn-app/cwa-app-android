package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule

internal val logicVaccinationDose = ObjectMapper().readTree(
    """{"and":[{">":[{"var":"payload.v.0.dn"},0]},{">=":[{"var":"payload.v.0.dn"},{"var":"payload.v.0.sd"}]}]}"""
)

internal val logicExactlyOne = ObjectMapper().readTree(
    """{
      "===": [
        {
          "reduce": [
            [
              {
                "var": "payload.r"
              },
              {
                "var": "payload.t"
              },
              {
                "var": "payload.v"
              }
            ],
            {
              "+": [
                {
                  "var": "accumulator"
                },
                {
                  "if": [
                    {
                      "var": "current.0"
                    },
                    1,
                    0
                  ]
                }
              ]
            },
            0
          ]
        },
        1
      ]
    }
  }
    """.trimIndent()
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
    logic = logicVaccinationDose
)

internal fun createGeneralRule(
    validFrom: String = "2021-05-27T07:46:40Z",
    validTo: String = "2022-08-01T07:46:40Z",
) = DccValidationRule(
    identifier = "GR-DE-1",
    version = "1.0.0",
    schemaVersion = "1.0.0",
    engine = "CERTLOGIC",
    engineVersion = "1.0.0",
    typeDcc = DccValidationRule.Type.ACCEPTANCE,
    country = "DE",
    certificateType = GENERAL,
    description = mapOf("en" to "Exactly one type of event."),
    validFrom = validFrom,
    validTo = validTo,
    affectedFields = listOf("payload.ver"),
    logic = logicExactlyOne
)
