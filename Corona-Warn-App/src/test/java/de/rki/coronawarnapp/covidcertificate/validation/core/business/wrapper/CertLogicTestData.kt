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

fun createVaccinationRule(
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

fun createGeneralRule(
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

internal val countryCodes = listOf(
    "AD",
    "AE",
    "AF",
    "AG",
    "AI",
    "AL",
    "AM",
    "AO",
    "AQ",
    "AR",
    "AS",
    "AT",
    "AU",
    "AW",
    "AX",
    "AZ",
    "BA",
    "BB",
    "BD",
    "BE",
    "BF",
    "BG",
    "BH",
    "BI",
    "BJ",
    "BL",
    "BM",
    "BN",
    "BO",
    "BQ",
    "BR",
    "BS",
    "BT",
    "BV",
    "BW",
    "BY",
    "BZ",
    "CA",
    "CC",
    "CD",
    "CF",
    "CG",
    "CH",
    "CI",
    "CK",
    "CL",
    "CM",
    "CN",
    "CO",
    "CR",
    "CU",
    "CV",
    "CW",
    "CX",
    "CY",
    "CZ",
    "DE",
    "DJ",
    "DK",
    "DM",
    "DO",
    "DZ",
    "EC",
    "EE",
    "EG",
    "EH",
    "ER",
    "ES",
    "ET",
    "FI",
    "FJ",
    "FK",
    "FM",
    "FO",
    "FR",
    "GA",
    "GB",
    "GD",
    "GE",
    "GF",
    "GG",
    "GH",
    "GI",
    "GL",
    "GM",
    "GN",
    "GP",
    "GQ",
    "GR",
    "GS",
    "GT",
    "GU",
    "GW",
    "GY",
    "HK",
    "HM",
    "HN",
    "HR",
    "HT",
    "HU",
    "ID",
    "IE",
    "IL",
    "IM",
    "IN",
    "IO",
    "IQ",
    "IR",
    "IS",
    "IT",
    "JE",
    "JM",
    "JO",
    "JP",
    "KE",
    "KG",
    "KH",
    "KI",
    "KM",
    "KN",
    "KP",
    "KR",
    "KW",
    "KY",
    "KZ",
    "LA",
    "LB",
    "LC",
    "LI",
    "LK",
    "LR",
    "LS",
    "LT",
    "LU",
    "LV",
    "LY",
    "MA",
    "MC",
    "MD",
    "ME",
    "MF",
    "MG",
    "MH",
    "MK",
    "ML",
    "MM",
    "MN",
    "MO",
    "MP",
    "MQ",
    "MR",
    "MS",
    "MT",
    "MU",
    "MV",
    "MW",
    "MX",
    "MY",
    "MZ",
    "NA",
    "NC",
    "NE",
    "NF",
    "NG",
    "NI",
    "NL",
    "NO",
    "NP",
    "NR",
    "NU",
    "NZ",
    "OM",
    "PA",
    "PE",
    "PF",
    "PG",
    "PH",
    "PK",
    "PL",
    "PM",
    "PN",
    "PR",
    "PS",
    "PT",
    "PW",
    "PY",
    "QA",
    "RE",
    "RO",
    "RS",
    "RU",
    "RW",
    "SA",
    "SB",
    "SC",
    "SD",
    "SE",
    "SG",
    "SH",
    "SI",
    "SJ",
    "SK",
    "SL",
    "SM",
    "SN",
    "SO",
    "SR",
    "SS",
    "ST",
    "SV",
    "SX",
    "SY",
    "SZ",
    "TC",
    "TD",
    "TF",
    "TG",
    "TH",
    "TJ",
    "TK",
    "TL",
    "TM",
    "TN",
    "TO",
    "TR",
    "TT",
    "TV",
    "TW",
    "TZ",
    "UA",
    "UG",
    "UM",
    "US",
    "UY",
    "UZ",
    "VA",
    "VC",
    "VE",
    "VG",
    "VI",
    "VN",
    "VU",
    "WF",
    "WS",
    "YE",
    "YT",
    "ZA",
    "ZM",
    "ZW"
)

internal val countryCodeMap = Pair(
    "country-2-codes",
    countryCodes
)
