package de.rki.coronawarnapp.dccticketing.core.common

import com.fasterxml.jackson.databind.ObjectMapper
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingResultToken
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class JwtTokenParserTest : BaseTest() {

    @Test
    fun `Valid JWT token should be parsed`() {
        val token =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJodHRwczovL2RnY2EtYm9va2luZy1kZW1vLWV1LXRlc3QuY2ZhcHBzLmV1MTAuaGFuYS5vbmRlbWFuZC5jb20vYXBpL2lkZW50aXR5IiwiZXhwIjoxNjM1ODQwOTQ0LCJzdWIiOiI5NThhM2ViMi01MDNjLTQ1YWYtYTg1NS00ZmViYjQ3NTg2YjIiLCJhdWQiOiJodHRwczovL2RnY2EtdmFsaWRhdGlvbi1zZXJ2aWNlLWV1LWFjYy5jZmFwcHMuZXUxMC5oYW5hLm9uZGVtYW5kLmNvbS92YWxpZGF0ZS85NThhM2ViMi01MDNjLTQ1YWYtYTg1NS00ZmViYjQ3NTg2YjIiLCJ0IjoyLCJ2IjoiMS4wIiwiaWF0IjoxNjM1ODM3MzQ0LCJ2YyI6eyJsYW5nIjoiZW4tZW4iLCJmbnQiOiJXVVJTVCIsImdudCI6IkhBTlMiLCJkb2IiOiIxOTkwLTAxLTAxIiwiY29hIjoiQUYiLCJjb2QiOiJTSiIsInJvYSI6IkFGIiwicm9kIjoiU0oiLCJ0eXBlIjpbInIiLCJ2IiwidCJdLCJjYXRlZ29yeSI6WyJTdGFuZGFyZCJdLCJ2YWxpZGF0aW9uQ2xvY2siOiIyMDIxLTExLTAzVDE1OjM5OjQzKzAwOjAwIiwidmFsaWRGcm9tIjoiMjAyMS0xMS0wM1QwNzoxNTo0MyswMDowMCIsInZhbGlkVG8iOiIyMDIxLTExLTAzVDE1OjM5OjQzKzAwOjAwIn0sImp0aSI6ImFkZGRkYTU2LWE3ZDItNDY1Ny1hMzk1LWM4YjJkZDNmNTI2NCJ9.nqlpekU7VJLQLeIcc7TPx9sYj0HD95Fgg-wUbfpv1-E"
        getInstance().getAccessToken(token) shouldBe DccTicketingAccessToken(
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
    }

    @Test
    fun `Invalid JWT should throw an exception`() {
        shouldThrow<Exception> {
            getInstance().getAccessToken("")
        }
    }

    @Test
    fun `Invalid JSON body shouldn't be parsed`() {
        getInstance().getAccessToken("A.B.C") shouldBe null
    }

    @Test
    fun `getResultToken - Invalid JWT should throw an exception`() {
        shouldThrow<Exception> {
            getInstance().getResultToken("")
        }
    }

    @Test
    fun `getResultToken - Invalid JSON body shouldn't be parsed`() {
        getInstance().getResultToken("A.B.C") shouldBe null
    }

    @Test
    fun `getResultToken - getResultToken - Valid JWT token should be parsed`() {
        val token = """
            eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InBHV3F6QjlCeldZPSJ9.eyJzdWIiOiIwNGM4OGE3My04YzZmLTQ5YzktYmE0Mi00NWY0ZmFmODA5YjUiLCJpc3MiOiIiLCJleHAiOjE2MzgzMDk1MjQsImNhdGVnb3J5IjpbIlN0YW5kYXJkIl0sImNvbmZpcm1hdGlvbiI6ImV5SmhiR2NpT2lKRlV6STFOaUlzSW5SNWNDSTZJa3BYVkNJc0ltdHBaQ0k2SW5CSFYzRjZRamxDZWxkWlBTSjkuZXlKcFlYUWlPakUyTXpneU1qTTNNRFY5LmVCM0gzZ3hsaXA0RFUwTGVzQVRZVEdNM2hpX3JIX2ZVb3k3UWNLT2daYWRfT01SX2NpWU9NblRfVW5TeHFzSThaaTBEdERnRmZWU0Z2LXFpYVVTS1pBIiwicmVzdWx0cyI6W10sInJlc3VsdCI6Ik9LIiwiaWF0IjoxNjM4MjIzNzA1fQ.UcxMoxWkQMTt6Dzz5WbttqOumu3C_d_hdSuu6_ic-dDF6Rys62Y-pC9BFe2D_Oo6s3FSjWwCWFqYBbQQ-w5vKA
        """.trimIndent()
        getInstance().getResultToken(token).result shouldBe DccTicketingResultToken.DccResult.PASS
    }

    private fun getInstance() = JwtTokenParser(JwtTokenConverter(ObjectMapper()))
}
