package de.rki.coronawarnapp.dccticketing.core.common

import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingAccessToken
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class JwtTokenValidatorKtTest : BaseTest() {

    @MockK lateinit var dccTicketingAccessToken: DccTicketingAccessToken

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `valid DccTicketingAccessToken shouldn't throw an exception`() {
        every { dccTicketingAccessToken.t } returns 1
        every { dccTicketingAccessToken.aud } returns "http://localhost/"
        shouldNotThrow<Exception> {
            dccTicketingAccessToken.validate()
        }
    }

    @Test
    fun `invalid DccTicketingAccessToken should throw an exception`() {
        every { dccTicketingAccessToken.t } returns 0
        every { dccTicketingAccessToken.aud } returns "http://localhost/"
        shouldThrow<DccTicketingException> {
            dccTicketingAccessToken.validate()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_TYPE_INVALID

        every { dccTicketingAccessToken.t } returns 1
        every { dccTicketingAccessToken.aud } returns ""
        shouldThrow<DccTicketingException> {
            dccTicketingAccessToken.validate()
        }.errorCode shouldBe DccTicketingException.ErrorCode.ATR_AUD_INVALID
    }
}
