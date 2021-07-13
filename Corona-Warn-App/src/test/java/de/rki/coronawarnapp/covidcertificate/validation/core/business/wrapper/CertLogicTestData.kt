@file:Suppress("LongParameterList")
package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Description
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Type
import dgca.verifier.app.engine.data.CertificateType

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

fun createDccRule(
    certificateType: CertificateType,
    identifier: String = when (certificateType) {
        CertificateType.GENERAL -> "GR-DE-1"
        CertificateType.TEST -> "TR-DE-1"
        CertificateType.VACCINATION -> "VR-DE-1"
        CertificateType.RECOVERY -> "RR-DE-1"
    },
    validFrom: String = "2021-05-27T07:46:40Z",
    validTo: String = "2022-08-01T07:46:40Z",
    version: String = "1.0.0",
    country: String = "DE",
) = DccValidationRule(
    identifier = identifier,
    version = version,
    schemaVersion = "1.0.0",
    engine = "CERTLOGIC",
    engineVersion = "1.0.0",
    typeDcc = Type.ACCEPTANCE,
    country = country,
    certificateType = certificateType.name,
    description = when (certificateType) {
        CertificateType.GENERAL -> listOf(Description("en", "Exactly one type of event."))
        CertificateType.TEST -> listOf(Description("en", "Test is outdated."))
        CertificateType.VACCINATION -> listOf(Description("en", "Vaccination must be complete."))
        CertificateType.RECOVERY -> throw NotImplementedError()
    },
    validFrom = validFrom,
    validTo = validTo,
    affectedFields = when (certificateType) {
        CertificateType.GENERAL -> listOf("payload.ver")
        CertificateType.TEST -> listOf("t.0.sc")
        CertificateType.VACCINATION -> listOf("v.0.dn", "v.0.sd")
        CertificateType.RECOVERY -> throw NotImplementedError()
    },
    logic = when (certificateType) {
        CertificateType.GENERAL -> logicExactlyOne
        CertificateType.TEST -> logicExactlyOne // TODO
        CertificateType.VACCINATION -> logicVaccinationDose
        CertificateType.RECOVERY -> throw NotImplementedError()
    }
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
