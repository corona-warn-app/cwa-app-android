package de.rki.coronawarnapp.covidcertificate.validation.core.server

import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountryApi
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleApi
import de.rki.coronawarnapp.exception.http.CwaUnknownHostException
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Cache
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import okio.IOException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Response
import testhelpers.BaseIOTest
import testhelpers.TestDispatcherProvider

@Suppress("MaxLineLength")
class DccValidationServerTest : BaseIOTest() {

    @MockK lateinit var countryApi: DccCountryApi
    @MockK lateinit var rulesApi: DccValidationRuleApi
    @MockK lateinit var signatureValidation: SignatureValidation
    @MockK lateinit var cache: Cache

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { signatureValidation.hasValidSignature(any(), any()) } returns true
        every { cache.evictAll() } just Runs
        coEvery { countryApi.onboardedCountries() } returns Response.success(COUNTRY_ARCHIVE.toResponseBody())
    }

    private fun createInstance() = spyk(
        DccValidationServer(
            countryApiLazy = { countryApi },
            rulesApiLazy = { rulesApi },
            signatureValidation = signatureValidation,
            cache = cache,
            dispatcherProvider = TestDispatcherProvider()
        )
    )

    @Test
    fun `successful download`() = runBlockingTest {

        val server = createInstance()

        val rawCountries = server.dccCountryJson()
        rawCountries shouldBe "[\"DE\",\"EU\",\"DX\",\"AT\",\"BG\",\"LU\",\"SE\",\"LT\",\"HR\",\"NL\",\"CZ\",\"EE\",\"ES\",\"MT\",\"IT\",\"CY\",\"IS\",\"FR\",\"GR\",\"RO\",\"PL\",\"DK\",\"SI\",\"PT\",\"LV\",\"MM\",\"SK\",\"CH\",\"FI\",\"HU\",\"SM\",\"VA\",\"BE\",\"EL\",\"IE\"]"

        verify(exactly = 1) { signatureValidation.hasValidSignature(any(), any()) }
    }

    @Test
    fun `successful download of booster notification rule set form server`() = runBlockingTest {

        val server = createInstance()

        // first we get a new rule set from the server
        val networkResponseMock: okhttp3.Response = mockk {
            every { code } returns 200
            every { body } returns BOOSTER_RULESET_ARCHIVE.toResponseBody()
        }
        val rawResponseMock: okhttp3.Response = mockk {
            every { code } returns 200
            every { isSuccessful } returns true
            every { cacheResponse } returns null
            every { networkResponse } returns networkResponseMock
        }
        coEvery { rulesApi.boosterNotificationRules() } returns Response.success(
            BOOSTER_RULESET_ARCHIVE.toResponseBody(),
            rawResponseMock
        )
        server.ruleSetJson(DccValidationRule.Type.BOOSTER_NOTIFICATION).apply {
            ruleSetJson.startsWith("[{\"Type\":\"BoosterNotification\",\"Logic\":{\"and\":[{\"not-before") shouldBe true
            source shouldBe DccValidationServer.RuleSetSource.SERVER
        }

        // next we get a 304 response from the server, so RuleSetSource.CACHE should be returned
        val response: okhttp3.Response = mockk {
            every { code } returns 304
            every { body } returns BOOSTER_RULESET_ARCHIVE.toResponseBody()
        }
        val cachedResponse: okhttp3.Response = mockk {
            every { isSuccessful } returns true
            every { networkResponse } returns response
        }

        coEvery { rulesApi.boosterNotificationRules() } returns Response.success(
            BOOSTER_RULESET_ARCHIVE.toResponseBody(),
            cachedResponse
        )

        every { server.getSource(any()) } returns DccValidationServer.RuleSetSource.CACHE
        server.ruleSetJson(DccValidationRule.Type.BOOSTER_NOTIFICATION).apply {
            ruleSetJson.startsWith("[{\"Type\":\"BoosterNotification\",\"Logic\":{\"and\":[{\"not-before") shouldBe true
            source shouldBe DccValidationServer.RuleSetSource.CACHE
        }

        verify(exactly = 2) { signatureValidation.hasValidSignature(any(), any()) }
    }

    @Test
    fun `country data is faulty`() = runBlockingTest {
        coEvery { countryApi.onboardedCountries() } returns Response.success("123ABC".decodeHex().toResponseBody())

        val server = createInstance()

        shouldThrow<DccValidationException> {
            server.dccCountryJson()
        }.errorCode shouldBe DccValidationException.ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_FILE_MISSING
    }

    @Test
    fun `acceptance rules data is faulty`() = runBlockingTest {
        coEvery { rulesApi.acceptanceRules() } returns Response.success("123ABC".decodeHex().toResponseBody())

        val server = createInstance()

        shouldThrow<DccValidationException> {
            server.ruleSetJson(DccValidationRule.Type.ACCEPTANCE)
        }.errorCode shouldBe DccValidationException.ErrorCode.ACCEPTANCE_RULE_JSON_ARCHIVE_FILE_MISSING
    }

    @Test
    fun `booster notification rules data is faulty`() = runBlockingTest {
        coEvery { rulesApi.boosterNotificationRules() } returns Response.success("123ABC".decodeHex().toResponseBody())

        val server = createInstance()

        shouldThrow<DccValidationException> {
            server.ruleSetJson(DccValidationRule.Type.BOOSTER_NOTIFICATION)
        }.errorCode shouldBe DccValidationException.ErrorCode.BOOSTER_NOTIFICATION_RULE_JSON_ARCHIVE_FILE_MISSING
    }

    @Test
    fun `invalidation rules data is faulty`() = runBlockingTest {
        coEvery { rulesApi.invalidationRules() } returns Response.success("123ABC".decodeHex().toResponseBody())

        val server = createInstance()

        shouldThrow<DccValidationException> {
            server.ruleSetJson(DccValidationRule.Type.INVALIDATION)
        }.errorCode shouldBe DccValidationException.ErrorCode.INVALIDATION_RULE_JSON_ARCHIVE_FILE_MISSING
    }

    @Test
    fun `verification fails`() = runBlockingTest {
        every { signatureValidation.hasValidSignature(any(), any()) } returns false

        val server = createInstance()

        shouldThrow<DccValidationException> {
            server.dccCountryJson()
        }.errorCode shouldBe DccValidationException.ErrorCode.ONBOARDED_COUNTRIES_JSON_ARCHIVE_SIGNATURE_INVALID
    }

    @Test
    fun `dcc countries maps network error`() = runBlockingTest {
        coEvery { countryApi.onboardedCountries() } throws CwaUnknownHostException(cause = IOException())

        shouldThrow<DccValidationException> {
            createInstance().dccCountryJson()
        }.errorCode shouldBe DccValidationException.ErrorCode.NO_NETWORK
    }

    @Test
    fun `acceptance rules maps network error`() = runBlockingTest {
        coEvery { rulesApi.acceptanceRules() } throws CwaUnknownHostException(cause = IOException())

        shouldThrow<DccValidationException> {
            createInstance().ruleSetJson(DccValidationRule.Type.ACCEPTANCE)
        }.errorCode shouldBe DccValidationException.ErrorCode.NO_NETWORK
    }

    @Test
    fun `booster notification rules maps network error`() = runBlockingTest {
        coEvery { rulesApi.boosterNotificationRules() } throws CwaUnknownHostException(cause = IOException())

        shouldThrow<DccValidationException> {
            createInstance().ruleSetJson(DccValidationRule.Type.BOOSTER_NOTIFICATION)
        }.errorCode shouldBe DccValidationException.ErrorCode.NO_NETWORK
    }

    @Test
    fun `invalidation rules maps network error`() = runBlockingTest {
        coEvery { rulesApi.invalidationRules() } throws CwaUnknownHostException(cause = IOException())

        shouldThrow<DccValidationException> {
            createInstance().ruleSetJson(DccValidationRule.Type.INVALIDATION)
        }.errorCode shouldBe DccValidationException.ErrorCode.NO_NETWORK
    }

    @Test
    fun `clear clears cache`() = runBlockingTest {
        createInstance().reset()
        verify { cache.evictAll() }
    }

    companion object {
        private val COUNTRY_ARCHIVE =
            "UEsDBBQACAgIAA5r3lIAAAAAAAAAAAAAAAAKAAAAZXhwb3J0LnNpZwGJAHb/CoYBCjgKGGRlLnJraS5jb3JvbmF3YXJuYXBwLWRldhoCdjEiAzI2MioTMS4yLjg0MC4xMDA0NS40LjMuMhABGAEiRjBEAiATa0Si6qXzbaM6J3+an/5qEk/sI+qo/FJDYxFC5V0qFAIgeYdoydE/IkXqgCMYxrOIz9lBxVakyjDaB6LJbD44i8RQSwcIE4NHkY4AAACJAAAAUEsDBBQACAgIAA5r3lIAAAAAAAAAAAAAAAAKAAAAZXhwb3J0LmJpbg3MURJAMAwA0cM4CW1oRoppwuBzT+W4+r9vv4EsyEm+GYNpwU5csKA0NiO9SA+cGmiQHtSZG0uj7RxGXnHlCOyiVnwlFWal9E/lGpk6N1R+UEsHCFoVcatQAAAAawAAAFBLAQIUABQACAgIAA5r3lITg0eRjgAAAIkAAAAKAAAAAAAAAAAAAAAAAAAAAABleHBvcnQuc2lnUEsBAhQAFAAICAgADmveUloVcatQAAAAawAAAAoAAAAAAAAAAAAAAAAAxgAAAGV4cG9ydC5iaW5QSwUGAAAAAAIAAgBwAAAATgEAAAAA"
                .decodeBase64()!!

        private val BOOSTER_RULESET_ARCHIVE =
            "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"
                .decodeBase64()!!
    }
}
