package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class JwtTokenConverterTest : BaseTest() {

    private val serializationModule = SerializationModule()
    private val baseGson = serializationModule.baseGson()
    private val converter = JwtTokenConverter(baseGson)

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
}
