package de.rki.coronawarnapp.qrcode.handler

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.RestoreDuplicateTest
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.TestPending
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.TestInvalid
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.InRecycleBin
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.TestNegative
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.WarnOthers
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.TestPositive
import de.rki.coronawarnapp.qrcode.ui.CoronaTestResult.TestRegistrationSelection
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestQRCodeHandlerTest : BaseTest() {

    @RelaxedMockK lateinit var recycledCoronaTestsProvider: RecycledCoronaTestsProvider
    @MockK lateinit var submissionRepository: SubmissionRepository

    private val instance: CoronaTestQRCodeHandler
        get() = CoronaTestQRCodeHandler(
            recycledCoronaTestsProvider = recycledCoronaTestsProvider,
            submissionRepository = submissionRepository
        )

    private val recycledRAT = RACoronaTest(
        identifier = "rat-identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.RAT_REDEEMED,
        testedAt = Instant.EPOCH,
        isDccConsentGiven = false,
        isDccSupportedByPoc = false,
    )

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

    private val recycledPCR = PCRCoronaTest(
        identifier = "pcr-identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.PCR_NEGATIVE,
        isDccConsentGiven = true
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

        coEvery { submissionRepository.testForType(any()) } returns flowOf(null)
    }

    @Test
    fun `restoreCoronaTest PCR test when another PCR is active`() = runBlockingTest {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(anotherPCR)

        with(instance) {
            restoreCoronaTest(recycledPCR) shouldBe RestoreDuplicateTest(recycledPCR.toRestoreRecycledTestRequest())
        }

        coVerify(exactly = 0) { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test when another RAT is active`() = runBlockingTest {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(anotherRAT)

        with(instance) {
            restoreCoronaTest(recycledRAT) shouldBe RestoreDuplicateTest(recycledRAT.toRestoreRecycledTestRequest())
        }

        coVerify(exactly = 0) { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is pending`() = runBlockingTest {
        val recycledCoronaTest = recycledPCR.copy(testResult = CoronaTestResult.PCR_OR_RAT_PENDING)

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe TestPending(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is pending`() = runBlockingTest {
        val recycledCoronaTest = recycledRAT.copy(testResult = CoronaTestResult.PCR_OR_RAT_PENDING)

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe TestPending(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is negative`() = runBlockingTest {
        val recycledCoronaTest = recycledPCR.copy(testResult = CoronaTestResult.PCR_NEGATIVE)

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe TestNegative(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is negative`() = runBlockingTest {
        val recycledCoronaTest = recycledRAT.copy(testResult = CoronaTestResult.RAT_NEGATIVE)

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe TestNegative(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is invalid`() = runBlockingTest {
        val recycledCoronaTest = recycledPCR.copy(testResult = CoronaTestResult.PCR_INVALID)

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe TestInvalid(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is invalid`() = runBlockingTest {
        val recycledCoronaTest = recycledRAT.copy(testResult = CoronaTestResult.RAT_INVALID)

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe TestInvalid(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is positive - warn other consent given`() = runBlockingTest {
        val recycledCoronaTest = recycledPCR.copy(
            testResult = CoronaTestResult.PCR_POSITIVE,
            isAdvancedConsentGiven = true
        )

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe TestPositive(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is positive - warn other consent given`() = runBlockingTest {
        val recycledCoronaTest = recycledRAT.copy(
            testResult = CoronaTestResult.RAT_POSITIVE,
            isAdvancedConsentGiven = true
        )

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe TestPositive(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is positive - warn other consent not given`() = runBlockingTest {
        val recycledCoronaTest = recycledPCR.copy(
            testResult = CoronaTestResult.PCR_POSITIVE,
            isAdvancedConsentGiven = false
        )

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe WarnOthers(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest RAT test is positive - warn other consent not given`() = runBlockingTest {
        val recycledCoronaTest = recycledRAT.copy(
            testResult = CoronaTestResult.RAT_POSITIVE,
            isAdvancedConsentGiven = false
        )

        with(instance) {
            restoreCoronaTest(recycledCoronaTest) shouldBe WarnOthers(recycledCoronaTest)
        }

        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `restoreCoronaTest PCR test is not pending`() = runBlockingTest {
        instance.restoreCoronaTest(recycledPCR)
        coVerify { recycledCoronaTestsProvider.restoreCoronaTest(any()) }
    }

    @Test
    fun `handle PCR in recycle bin`() = runBlockingTest {
        val hash = coronaTestQrCodePCR.rawQrCode.toSHA256()
        coEvery { recycledCoronaTestsProvider.findCoronaTest(hash) } returns anotherPCR

        with(instance) {
            handleQrCode(coronaTestQrCodePCR) shouldBe InRecycleBin(anotherPCR)
        }

        coVerify {
            recycledCoronaTestsProvider.findCoronaTest(hash)
            submissionRepository wasNot called
        }
    }

    @Test
    fun `handle RAT in recycle bin`() = runBlockingTest {
        val hash = coronaTestQrCodeRAT.rawQrCode.toSHA256()
        coEvery { recycledCoronaTestsProvider.findCoronaTest(hash) } returns anotherRAT

        with(instance) {
            handleQrCode(coronaTestQrCodeRAT) shouldBe InRecycleBin(anotherRAT)
        }

        coVerify {
            recycledCoronaTestsProvider.findCoronaTest(hash)
            submissionRepository wasNot called
        }
    }

    @Test
    fun `handle new PCR test`() = runBlockingTest {
        val hash = coronaTestQrCodePCR.rawQrCode.toSHA256()
        coEvery { recycledCoronaTestsProvider.findCoronaTest(any()) } returns null

        with(instance) {
            handleQrCode(coronaTestQrCodePCR) shouldBe TestRegistrationSelection(coronaTestQrCodePCR)
        }

        coVerify {
            recycledCoronaTestsProvider.findCoronaTest(hash)
        }
    }

    @Test
    fun `handle new RAT test`() = runBlockingTest {
        val hash = coronaTestQrCodeRAT.rawQrCode.toSHA256()
        coEvery { recycledCoronaTestsProvider.findCoronaTest(any()) } returns null

        with(instance) {
            handleQrCode(coronaTestQrCodeRAT) shouldBe TestRegistrationSelection(coronaTestQrCodeRAT)
        }

        coVerify {
            recycledCoronaTestsProvider.findCoronaTest(hash)
        }
    }
}
