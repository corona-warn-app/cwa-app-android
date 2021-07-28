@file:Suppress("LongParameterList")
package de.rki.coronawarnapp.covidcertificate.validation.core.business.wrapper

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Description
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule.Type
import dgca.verifier.app.engine.data.RuleCertificateType

internal val logicVaccinationDose = ObjectMapper().readTree(
    """{"and":[{">":[{"var":"payload.v.0.dn"},0]},{">=":[{"var":"payload.v.0.dn"},{"var":"payload.v.0.sd"}]}]}"""
)

@Suppress("MaxLineLength")
internal val logicPcr72hoursValid = ObjectMapper().readTree(
    """{"if":[{"var":"payload.t.0"},{"if":[{"===":[{"var":"payload.t.0.tt"},"LP6464-4"]},{"not-after":[{"plusTime":[{"var":"external.validationClock"},0,"day"]},{"plusTime":[{"var":"payload.t.0.sc"},72,"hour"]}]},true]},true]}"""
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
    certificateType: RuleCertificateType,
    identifier: String = when (certificateType) {
        RuleCertificateType.GENERAL -> "GR-DE-1"
        RuleCertificateType.TEST -> "TR-DE-1"
        RuleCertificateType.VACCINATION -> "VR-DE-1"
        RuleCertificateType.RECOVERY -> "RR-DE-1"
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
        RuleCertificateType.GENERAL -> listOf(Description("en", "Exactly one type of event."))
        RuleCertificateType.TEST -> listOf(Description("en", "PCR Test must not be older than 72h."))
        RuleCertificateType.VACCINATION -> listOf(Description("en", "Vaccination must be complete."))
        RuleCertificateType.RECOVERY -> throw NotImplementedError()
    },
    validFrom = validFrom,
    validTo = validTo,
    affectedFields = when (certificateType) {
        RuleCertificateType.GENERAL -> listOf("payload.ver")
        RuleCertificateType.TEST -> listOf("t.0.sc")
        RuleCertificateType.VACCINATION -> listOf("v.0.dn", "v.0.sd")
        RuleCertificateType.RECOVERY -> throw NotImplementedError()
    },
    logic = when (certificateType) {
        RuleCertificateType.GENERAL -> logicExactlyOne
        RuleCertificateType.TEST -> logicPcr72hoursValid
        RuleCertificateType.VACCINATION -> logicVaccinationDose
        RuleCertificateType.RECOVERY -> throw NotImplementedError()
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
