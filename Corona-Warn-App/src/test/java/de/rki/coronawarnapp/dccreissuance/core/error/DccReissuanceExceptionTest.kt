package de.rki.coronawarnapp.dccreissuance.core.error

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException.ErrorCode
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException.TextKey
import de.rki.coronawarnapp.dccreissuance.core.server.data.DccReissuanceErrorResponse
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

@Suppress("MaxLineLength")
class DccReissuanceExceptionTest : BaseTest() {

    @RelaxedMockK lateinit var context: Context

    private val contactSupport = "contact support"
    private val noNetwork = "no network"
    private val tryAgain = "try again"
    private val reissuanceNotSupported = "reissuance not supported"

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every {
            context.getString(R.string.dcc_reissuance_error_handling_text_key_contact_support)
        } returns contactSupport
        every {
            context.getString(R.string.dcc_reissuance_error_handling_text_key_no_network)
        } returns noNetwork
        every {
            context.getString(R.string.dcc_reissuance_error_handling_text_key_try_again)
        } returns tryAgain
        every {
            context.getString(R.string.dcc_reissuance_error_handling_text_key_reissuance_not_supported)
        } returns reissuanceNotSupported
    }

    @Test
    fun `check error code text key mapping`() {
        ErrorCode.DCC_RI_PIN_MISMATCH.textKey shouldBe TextKey.CONTACT_SUPPORT
        ErrorCode.DCC_RI_PARSE_ERR.textKey shouldBe TextKey.CONTACT_SUPPORT

        ErrorCode.DCC_RI_NO_NETWORK.textKey shouldBe TextKey.NO_NETWORK

        ErrorCode.DCC_RI_400.textKey shouldBe TextKey.TRY_AGAIN
        ErrorCode.DCC_RI_406.textKey shouldBe TextKey.TRY_AGAIN
        ErrorCode.DCC_RI_429.textKey shouldBe TextKey.TRY_AGAIN
        ErrorCode.DCC_RI_500.textKey shouldBe TextKey.TRY_AGAIN
        ErrorCode.DCC_RI_CLIENT_ERR.textKey shouldBe TextKey.TRY_AGAIN
        ErrorCode.DCC_RI_SERVER_ERR.textKey shouldBe TextKey.TRY_AGAIN

        ErrorCode.DCC_RI_401.textKey shouldBe TextKey.REISSUANCE_NOT_SUPPORTED
        ErrorCode.DCC_RI_403.textKey shouldBe TextKey.REISSUANCE_NOT_SUPPORTED
    }

    @Test
    fun `check text key mapping`() {
        DccReissuanceException(errorCode = ErrorCode.DCC_RI_PIN_MISMATCH).errorMessage.get(context) shouldBe contactSupport
        DccReissuanceException(errorCode = ErrorCode.DCC_RI_NO_NETWORK).errorMessage.get(context) shouldBe noNetwork
        DccReissuanceException(errorCode = ErrorCode.DCC_RI_400).errorMessage.get(context) shouldBe tryAgain
        DccReissuanceException(errorCode = ErrorCode.DCC_RI_401).errorMessage.get(context) shouldBe reissuanceNotSupported
    }

    @Test
    fun `check message and human readable error`() {
        val errorCode = ErrorCode.DCC_RI_429
        val dccReissuanceErrorResponse = DccReissuanceErrorResponse(
            error = "RI400-1200",
            message = "certificates not acceptable for action"
        )

        DccReissuanceException(errorCode = errorCode, serverErrorResponse = dccReissuanceErrorResponse).run {
            message shouldBe "${errorCode.message} - $dccReissuanceErrorResponse"

            val description = toHumanReadableError(context).description
            description shouldBe "${errorMessage.get(context)} ($errorCode & ${dccReissuanceErrorResponse.error})"
        }

        DccReissuanceException(errorCode = errorCode).run {
            message shouldBe errorCode.message

            toHumanReadableError(context).description shouldBe "${errorMessage.get(context)} ($errorCode)"
        }
    }
}
