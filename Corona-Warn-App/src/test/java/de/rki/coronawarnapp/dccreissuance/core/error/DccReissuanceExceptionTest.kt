package de.rki.coronawarnapp.dccreissuance.core.error

import android.content.Context
import de.rki.coronawarnapp.R
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException.ErrorCode
import de.rki.coronawarnapp.dccreissuance.core.error.DccReissuanceException.TextKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class DccReissuanceExceptionTest : BaseTest() {

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
        val contactSupport = "contact support"
        val noNetwork = "no network"
        val tryAgain = "try again"
        val reissuanceNotSupported = "reissuance not supported"

        val context: Context = mockk {
            every { getString(R.string.dcc_reissuance_error_handling_text_key_contact_support) } returns contactSupport
            every { getString(R.string.dcc_reissuance_error_handling_text_key_no_network) } returns noNetwork
            every { getString(R.string.dcc_reissuance_error_handling_text_key_try_again) } returns tryAgain
            every { getString(R.string.dcc_reissuance_error_handling_text_key_reissuance_not_supported) } returns reissuanceNotSupported
        }

        DccReissuanceException(errorCode = ErrorCode.DCC_RI_PIN_MISMATCH).errorMessage.get(context) shouldBe contactSupport
        DccReissuanceException(errorCode = ErrorCode.DCC_RI_NO_NETWORK).errorMessage.get(context) shouldBe noNetwork
        DccReissuanceException(errorCode = ErrorCode.DCC_RI_400).errorMessage.get(context) shouldBe tryAgain
        DccReissuanceException(errorCode = ErrorCode.DCC_RI_401).errorMessage.get(context) shouldBe reissuanceNotSupported
    }
}
