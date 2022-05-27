package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestQRCodeHandlerTest : BaseTest() {

    @MockK lateinit var recycledCoronaTestsProvider: RecycledCoronaTestsProvider

    private val instance: CoronaTestQRCodeHandler
        get() = CoronaTestQRCodeHandler(recycledCoronaTestsProvider = recycledCoronaTestsProvider)

    private val anotherRAT = RACoronaTest(
        identifier = "rat-identifier-another",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token-another",
        testResult = CoronaTestResult.RAT_REDEEMED,
        testedAt = Instant.EPOCH,
        isDccConsentGiven = false,
        isDccSupportedByPoc = false
    )

    private val anotherPCR = PCRCoronaTest(
        identifier = "pcr-identifier-another",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token-another",
        testResult = CoronaTestResult.PCR_NEGATIVE,
        isDccConsentGiven = true
    )

    private val coronaTestQrCodePCR = CoronaTestQRCode.PCR(
        qrCodeGUID = "qrCodeGUID",
        rawQrCode = "rawQrCode"
    )

    private val coronaTestQrCodeRAT = CoronaTestQRCode.RapidAntigen(
        rawQrCode = "rawQrCode",
        hash = "hash",
        createdAt = Instant.EPOCH
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `handle PCR in recycle bin`() = runTest {
        val hash = coronaTestQrCodePCR.rawQrCode.toSHA256()
        coEvery { recycledCoronaTestsProvider.findCoronaTest(hash) } returns anotherPCR

        with(instance) {
            handleQrCode(coronaTestQrCodePCR) shouldBe CoronaTestQRCodeHandler.InRecycleBin(anotherPCR)
        }

        coVerify {
            recycledCoronaTestsProvider.findCoronaTest(hash)
        }
    }

    @Test
    fun `handle RAT in recycle bin`() = runTest {
        val hash = coronaTestQrCodeRAT.rawQrCode.toSHA256()
        coEvery { recycledCoronaTestsProvider.findCoronaTest(hash) } returns anotherRAT

        with(instance) {
            handleQrCode(coronaTestQrCodeRAT) shouldBe CoronaTestQRCodeHandler.InRecycleBin(anotherRAT)
        }

        coVerify {
            recycledCoronaTestsProvider.findCoronaTest(hash)
        }
    }

    @Test
    fun `handle new PCR test`() = runTest {
        val hash = coronaTestQrCodePCR.rawQrCode.toSHA256()
        coEvery { recycledCoronaTestsProvider.findCoronaTest(any()) } returns null

        with(instance) {
            handleQrCode(coronaTestQrCodePCR) shouldBe CoronaTestQRCodeHandler.TestRegistrationSelection(
                coronaTestQrCodePCR
            )
        }

        coVerify {
            recycledCoronaTestsProvider.findCoronaTest(hash)
        }
    }

    @Test
    fun `handle new RAT test`() = runTest {
        val hash = coronaTestQrCodeRAT.rawQrCode.toSHA256()
        coEvery { recycledCoronaTestsProvider.findCoronaTest(any()) } returns null

        with(instance) {
            handleQrCode(coronaTestQrCodeRAT) shouldBe CoronaTestQRCodeHandler.TestRegistrationSelection(
                coronaTestQrCodeRAT
            )
        }

        coVerify {
            recycledCoronaTestsProvider.findCoronaTest(hash)
        }
    }
}
