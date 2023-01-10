package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultItem
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class JwtTokenConverterTest : BaseTest() {

    private val serializationModule = SerializationModule()
    private val mapper = serializationModule.jacksonObjectMapper()
    private val converter = JwtTokenConverter(mapper)

    private val validJson = """
            {
               "iss": "https://dgca-booking-demo-eu-test.cfapps.eu10.hana.ondemand.com/api/identity",
               "exp": 1635840944,
               "sub": "958a3eb2-503c-45af-a855-4febb47586b2",
               "aud": "https://dgca-validation-service-eu-acc.cfapps.eu10.hana.ondemand.com/validate/958a3eb2-503c-45af-a855-4febb47586b2",
               "t": 2,
               "v": "1.0",
               "iat": 1635837344,
               "vc": {
                  "lang": "en-en",
                  "fnt": "WURST",
                  "gnt": "HANS",
                  "dob": "1990-01-01",
                  "coa": "AF",
                  "cod": "SJ",
                  "roa": "AF",
                  "rod": "SJ",
                  "type": [
                     "r",
                     "v",
                     "t"
                  ],
                  "category": [
                     "Standard"
                  ],
                  "validationClock": "2021-11-03T15:39:43+00:00",
                  "validFrom": "2021-11-03T07:15:43+00:00",
                  "validTo": "2021-11-03T15:39:43+00:00"
               },
               "jti": "adddda56-a7d2-4657-a395-c8b2dd3f5264"
            }
    """.trimIndent()

    private val jwtTokenObject = DccTicketingAccessToken(
        iss = "https://dgca-booking-demo-eu-test.cfapps.eu10.hana.ondemand.com/api/identity",
        exp = 1635840944,
        sub = "958a3eb2-503c-45af-a855-4febb47586b2",
        aud = "https://dgca-validation-service-eu-acc.cfapps.eu10.hana.ondemand.com/validate/958a3eb2-503c-45af-a855-4febb47586b2",
        t = 2,
        v = "1.0",
        iat = 1635837344,
        jti = "adddda56-a7d2-4657-a395-c8b2dd3f5264",
        vc = DccTicketingValidationCondition(
            lang = "en-en",
            fnt = "WURST",
            gnt = "HANS",
            dob = "1990-01-01",
            coa = "AF",
            cod = "SJ",
            roa = "AF",
            rod = "SJ",
            type = listOf(
                "r",
                "v",
                "t"
            ),
            category = listOf(
                "Standard"
            ),
            validationClock = "2021-11-03T15:39:43+00:00",
            validFrom = "2021-11-03T07:15:43+00:00",
            validTo = "2021-11-03T15:39:43+00:00",
            hash = null
        ),
    )

    @Test
    fun `test json to JwtToken conversion`() {
        converter.jsonToJwtToken(validJson) shouldBe jwtTokenObject
    }

    private val jsonResultToken = """
        {
           "sub": "1044236f-48df-43cb-8bdf-bed142e507ab",
           "iss": "https://dgca-validation-service-eu-acc.cfapps.eu10.hana.ondemand.com",
           "iat": 1635864502,
           "exp": 1635950902,
           "category": [
              "Standard"
           ],
           "confirmation": "eyJraWQiOiJSQU0yU3R3N0VrRT0iLCJhbGciOiJFUzI1NiJ9.eyJqdGkiOiJlMWU2YjU4MS1lN2NmLTQyZTAtYjM1ZS1jZmFhMTRkZTcxN2UiLCJzdWIiOiIxMDQ0MjM2Zi00OGRmLTQzY2ItOGJkZi1iZWQxNDJlNTA3YWIiLCJpc3MiOiJodHRwczovL2RnY2EtdmFsaWRhdGlvbi1zZXJ2aWNlLWV1LWFjYy5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbSIsImlhdCI6MTYzNTg2NDUwMiwiZXhwIjoxNjM1OTUwOTAyLCJyZXN1bHQiOiJOT0siLCJjYXRlZ29yeSI6WyJTdGFuZGFyZCJdfQ.OLnS59EWkpkZoEMfbyOs18dUauch9eaXxGK8Zrn-jo-S1kcgAxP8z8rdzLzNjCNTfi4CbVUnF6FV0lHuMnYBOw",
           "results": [
              {
                 "identifier": "KID",
                 "result": "NOK",
                 "type": "TechnicalVerification",
                 "details": "\"unknown dcc signing kid\""
              }
           ],
           "result": "NOK"
        }
    """.trimIndent()

    private val resultToken = DccTicketingResultToken(
        iss = "https://dgca-validation-service-eu-acc.cfapps.eu10.hana.ondemand.com",
        sub = "1044236f-48df-43cb-8bdf-bed142e507ab",
        iat = 1635864502,
        exp = 1635950902,
        category = listOf("Standard"),
        confirmation = "eyJraWQiOiJSQU0yU3R3N0VrRT0iLCJhbGciOiJFUzI1NiJ9.eyJqdGkiOiJlMWU2YjU4MS1lN2NmLTQyZTAtYjM1ZS1jZmFhMTRkZTcxN2UiLCJzdWIiOiIxMDQ0MjM2Zi00OGRmLTQzY2ItOGJkZi1iZWQxNDJlNTA3YWIiLCJpc3MiOiJodHRwczovL2RnY2EtdmFsaWRhdGlvbi1zZXJ2aWNlLWV1LWFjYy5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbSIsImlhdCI6MTYzNTg2NDUwMiwiZXhwIjoxNjM1OTUwOTAyLCJyZXN1bHQiOiJOT0siLCJjYXRlZ29yeSI6WyJTdGFuZGFyZCJdfQ.OLnS59EWkpkZoEMfbyOs18dUauch9eaXxGK8Zrn-jo-S1kcgAxP8z8rdzLzNjCNTfi4CbVUnF6FV0lHuMnYBOw",
        result = DccTicketingResultToken.DccResult.FAIL,
        results = listOf(
            DccTicketingResultItem(
                identifier = "KID",
                result = DccTicketingResultToken.DccResult.FAIL,
                type = "TechnicalVerification",
                details = "\"unknown dcc signing kid\""
            )
        )
    )

    @Test
    fun `test json to Result Token conversion`() {
        converter.jsonToResultToken(jsonResultToken) shouldBe resultToken
    }
}
