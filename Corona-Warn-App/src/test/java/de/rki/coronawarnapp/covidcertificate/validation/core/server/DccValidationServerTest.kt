package de.rki.coronawarnapp.covidcertificate.validation.core.server

import de.rki.coronawarnapp.covidcertificate.validation.core.common.exception.DccValidationException
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountryApi
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRuleApi
import de.rki.coronawarnapp.util.security.SignatureValidation
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.Cache
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
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

    private fun createInstance() = DccValidationServer(
        countryApiLazy = { countryApi },
        rulesApiLazy = { rulesApi },
        signatureValidation = signatureValidation,
        cache = cache,
        dispatcherProvider = TestDispatcherProvider()
    )

    @Test
    fun `successful download`() = runBlockingTest {

        val server = createInstance()

        val rawStatistics = server.dccCountryJson()
        rawStatistics shouldBe "[\"DE\",\"EU\",\"DX\",\"AT\",\"BG\",\"LU\",\"SE\",\"LT\",\"HR\",\"NL\",\"CZ\",\"EE\",\"ES\",\"MT\",\"IT\",\"CY\",\"IS\",\"FR\",\"GR\",\"RO\",\"PL\",\"DK\",\"SI\",\"PT\",\"LV\",\"MM\",\"SK\",\"CH\",\"FI\",\"HU\",\"SM\",\"VA\",\"BE\",\"EL\",\"IE\"]"

        verify(exactly = 1) { signatureValidation.hasValidSignature(any(), any()) }
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
    fun `clear clears cache`() {
        createInstance().clear()
        verify { cache.evictAll() }
    }

    companion object {
        private val COUNTRY_ARCHIVE =
            "UEsDBBQACAgIAA5r3lIAAAAAAAAAAAAAAAAKAAAAZXhwb3J0LnNpZwGJAHb/CoYBCjgKGGRlLnJraS5jb3JvbmF3YXJuYXBwLWRldhoCdjEiAzI2MioTMS4yLjg0MC4xMDA0NS40LjMuMhABGAEiRjBEAiATa0Si6qXzbaM6J3+an/5qEk/sI+qo/FJDYxFC5V0qFAIgeYdoydE/IkXqgCMYxrOIz9lBxVakyjDaB6LJbD44i8RQSwcIE4NHkY4AAACJAAAAUEsDBBQACAgIAA5r3lIAAAAAAAAAAAAAAAAKAAAAZXhwb3J0LmJpbg3MURJAMAwA0cM4CW1oRoppwuBzT+W4+r9vv4EsyEm+GYNpwU5csKA0NiO9SA+cGmiQHtSZG0uj7RxGXnHlCOyiVnwlFWal9E/lGpk6N1R+UEsHCFoVcatQAAAAawAAAFBLAQIUABQACAgIAA5r3lITg0eRjgAAAIkAAAAKAAAAAAAAAAAAAAAAAAAAAABleHBvcnQuc2lnUEsBAhQAFAAICAgADmveUloVcatQAAAAawAAAAoAAAAAAAAAAAAAAAAAxgAAAGV4cG9ydC5iaW5QSwUGAAAAAAIAAgBwAAAATgEAAAAA"
                .decodeBase64()!!
    }
}
