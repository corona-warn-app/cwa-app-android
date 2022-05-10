package de.rki.coronawarnapp.bugreporting.censors.dccticketing

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class DccTicketingJwtCensorTest : BaseTest() {

    private val rawJwt = "rawAccessToken"

    private val vc = DccTicketingValidationCondition(
        hash = "hash", // not censored
        lang = "lang", // not censored
        fnt = "family name",
        gnt = "given name",
        dob = "1981-03-20",
        type = listOf("v", "r"), // not censored
        coa = "ES",
        roa = "balearic islands",
        cod = "DE",
        rod = "Berlin",
        category = listOf("air travel", "train"),
        validationClock = "2021-11-30T12:58:23.052Z",
        validFrom = "2021-10-30T12:00:23.052Z",
        validTo = "2021-12-30T13:58:23.052Z",
    )

    @Test
    fun `censoring replaces raw JWT`() = runTest {
        val instance = DccTicketingJwtCensor()
        instance.addJwt(rawJwt)
        val filterMe = "Logging $rawJwt for debugging"
        instance.checkLog(filterMe)!!.compile()!!.censored shouldBe "Logging ticketing/rawJwt for debugging"
    }

    @Test
    fun `censoring replaces vc attributes`() = runTest {
        val instance = DccTicketingJwtCensor()

        instance.addVc(vc)
        val filterMe = "Logging $vc for debugging"
        val result = instance.checkLog(filterMe)!!.compile()!!.censored
        result shouldBe "Logging DccTicketingValidationCondition(hash=hash, lang=lang, fnt=ticketing/familyName, gnt=ticketing/givenName, dob=ticketing/dateOfBirth, type=[v, r], coa=ticketing/countryOfArrival, roa=ticketing/regionOfArrival, cod=ticketing/countryOfDeparture, rod=ticketing/regionOfDeparture, category=[ticketing/category0, ticketing/category1], validationClock=ticketing/validationClock, validFrom=ticketing/validFrom, validTo=ticketing/validTo) for debugging"
    }

    @Test
    fun `no censoring returns null`() = runTest {
        val instance = DccTicketingJwtCensor()
        instance.addVc(vc)
        instance.addJwt(rawJwt)
        val filterMe = "Nothing needs to be censored here"
        instance.checkLog(filterMe) shouldBe null
    }
}
