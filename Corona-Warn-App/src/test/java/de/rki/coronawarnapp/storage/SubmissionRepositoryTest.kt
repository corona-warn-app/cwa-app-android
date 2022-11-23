package de.rki.coronawarnapp.storage

import de.rki.coronawarnapp.coronatest.CoronaTestRepository
import de.rki.coronawarnapp.coronatest.errors.AlreadyRedeemedException
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.submission.SubmissionRepository
import de.rki.coronawarnapp.submission.SubmissionSettings
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant

class SubmissionRepositoryTest : BaseTest() {

    @MockK lateinit var submissionSettings: SubmissionSettings
    @MockK lateinit var coronaTestRepository: CoronaTestRepository

    private val pcrRegistrationRequest = CoronaTestQRCode.PCR(
        qrCodeGUID = "pcr-guid",
        rawQrCode = "rawQrCode"
    )
    private val pcrTest = PCRCoronaTest(
        identifier = pcrRegistrationRequest.identifier,
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.PCR_OR_RAT_REDEEMED,
    )

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coronaTestRepository.apply {
            every { coronaTests } returns emptyFlow()
            coEvery { registerTest(pcrRegistrationRequest, any(), any()) } returns pcrTest
        }

        submissionSettings.apply {
            every { symptoms } returns flowOf(null)
        }
    }

    fun createInstance(scope: CoroutineScope) = SubmissionRepository(
        scope = scope,
        submissionSettings = submissionSettings,
        coronaTestRepository = coronaTestRepository,
    )

    @Test
    fun `tryReplaceTest overrides register test conditions`() = runTest {
        val precondition = slot<(Collection<BaseCoronaTest>) -> Boolean>()
        val postcondition = slot<(BaseCoronaTest) -> Boolean>()

        val instance = createInstance(scope = this)

        instance.tryReplaceTest(pcrRegistrationRequest)

        coVerify { coronaTestRepository.registerTest(any(), capture(precondition), capture(postcondition)) }

        precondition.captured(emptyList()) shouldBe true
        precondition.captured(listOf(pcrTest)) shouldBe true

        shouldThrow<AlreadyRedeemedException> {
            postcondition.captured(pcrTest)
        }

        postcondition.captured(pcrTest.copy(testResult = CoronaTestResult.PCR_NEGATIVE)) shouldBe true
    }
}
