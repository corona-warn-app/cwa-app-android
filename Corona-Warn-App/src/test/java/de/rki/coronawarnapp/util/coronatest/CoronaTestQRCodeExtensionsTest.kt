package de.rki.coronawarnapp.util.coronatest

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.util.coroutine.modifyCategoryType
import io.kotest.matchers.shouldBe
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestQRCodeExtensionsTest : BaseTest() {

    @Test
    fun `modify category type pcr test`() {
        val request = CoronaTestQRCode.PCR(
            isDccConsentGiven = true,
            qrCodeGUID = "qrCodeGUID",
            rawQrCode = "rawQrCode"
        )

        val updated = request.modifyCategoryType(CoronaTestQRCode.CategoryType.FAMILY)
        with (updated) {
            isDccConsentGiven shouldBe true
            registrationIdentifier shouldBe "qrCodeGUID"
            rawQrCode shouldBe "rawQrCode"
            categoryType shouldBe CoronaTestQRCode.CategoryType.FAMILY
        }
    }

    @Test
    fun `modify category type rapid-antigen test`() {
        val date = Instant.now()
        val request = CoronaTestQRCode.RapidAntigen(
            isDccConsentGiven = true,
            isDccSupportedByPoc = true,
            rawQrCode = "rawQrCode",
            hash = "hash",
            createdAt = date,
            firstName = "FirstName",
            lastName = "LastName",
            testId = "TestId",
            salt = "saaaalty",
        )

        val updated = request.modifyCategoryType(CoronaTestQRCode.CategoryType.FAMILY)
        with (updated as CoronaTestQRCode.RapidAntigen) {
            isDccConsentGiven shouldBe true
            isDccSupportedByPoc shouldBe true
            rawQrCode shouldBe "rawQrCode"
            registrationIdentifier shouldBe "hash"
            createdAt shouldBe date
            firstName shouldBe "FirstName"
            lastName shouldBe "LastName"
            testId shouldBe "TestId"
            salt shouldBe "saaaalty"
            categoryType shouldBe CoronaTestQRCode.CategoryType.FAMILY
        }
    }

    @Test
    fun `modify category type rapid-pcr test`() {
        val date = Instant.now()
        val request = CoronaTestQRCode.RapidPCR(
            isDccConsentGiven = true,
            isDccSupportedByPoc = true,
            rawQrCode = "rawQrCode",
            hash = "hash",
            createdAt = date,
            firstName = "FirstName",
            lastName = "LastName",
            testId = "TestId",
            salt = "saaaalty",
        )

        val updated = request.modifyCategoryType(CoronaTestQRCode.CategoryType.FAMILY)
        with (updated as CoronaTestQRCode.RapidPCR) {
            isDccConsentGiven shouldBe true
            isDccSupportedByPoc shouldBe true
            rawQrCode shouldBe "rawQrCode"
            registrationIdentifier shouldBe "hash"
            createdAt shouldBe date
            firstName shouldBe "FirstName"
            lastName shouldBe "LastName"
            testId shouldBe "TestId"
            salt shouldBe "saaaalty"
            categoryType shouldBe CoronaTestQRCode.CategoryType.FAMILY
        }
    }
}
