package de.rki.coronawarnapp.recyclebin.coronatest.handler

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.reyclebin.coronatest.RecycledCoronaTestsProvider
import de.rki.coronawarnapp.reyclebin.coronatest.handler.CoronaTestRestoreEvent
import de.rki.coronawarnapp.reyclebin.coronatest.handler.CoronaTestRestoreHandler
import de.rki.coronawarnapp.reyclebin.coronatest.request.toRestoreRecycledTestRequest
import de.rki.coronawarnapp.submission.SubmissionRepository
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CoronaTestRestoreHandlerTest : BaseTest() {

    @RelaxedMockK lateinit var recycledCoronaTestsProvider: RecycledCoronaTestsProvider
    @MockK lateinit var submissionRepository: SubmissionRepository

    private val instance: CoronaTestRestoreHandler
        get() = CoronaTestRestoreHandler(
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

    private val familyCoronaTest = FamilyCoronaTest(
        personName = "personName",
        coronaTest = CoronaTest(
            type = BaseCoronaTest.Type.PCR,
            identifier = "family-pcr-identifier",
            registeredAt = Instant.EPOCH,
            registrationToken = "registrationToken"
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { submissionRepository.testForType(any()) } returns flowOf(null)
    }

    @Test
    fun `restore personal corona test PCR whether no other PCR is active`() = runTest {
        instance.restoreCoronaTest(recycledPCR, openResult = false) shouldBe CoronaTestRestoreEvent.RestoredTest(
            recycledPCR
        )

        coVerify {
            recycledCoronaTestsProvider.restoreCoronaTest(recycledPCR.identifier)
            submissionRepository.testForType(BaseCoronaTest.Type.PCR)
        }
    }

    @Test
    fun `restore personal corona test RAT whether no other RAT is active`() = runTest {
        instance.restoreCoronaTest(recycledRAT, openResult = false) shouldBe CoronaTestRestoreEvent.RestoredTest(
            recycledRAT
        )

        coVerify {
            recycledCoronaTestsProvider.restoreCoronaTest(recycledRAT.identifier)
            submissionRepository.testForType(BaseCoronaTest.Type.RAPID_ANTIGEN)
        }
    }

    @Test
    fun `restore family corona test PCR regardless of whether another PCR is active`() = runTest {
        every { submissionRepository.testForType(BaseCoronaTest.Type.PCR) } returns flowOf(recycledPCR)

        instance.restoreCoronaTest(familyCoronaTest, openResult = false) shouldBe CoronaTestRestoreEvent.RestoredTest(
            familyCoronaTest
        )

        coVerify {
            recycledCoronaTestsProvider.restoreCoronaTest(familyCoronaTest.identifier)
            submissionRepository wasNot Called
        }
    }

    @Test
    fun `show duplicate warning if another personal test PCR is active`() = runTest {
        every { submissionRepository.testForType(BaseCoronaTest.Type.PCR) } returns flowOf(anotherPCR)

        instance.restoreCoronaTest(
            recycledPCR,
            openResult = false
        ) shouldBe CoronaTestRestoreEvent.RestoreDuplicateTest(
            restoreRecycledTestRequest = recycledPCR.toRestoreRecycledTestRequest(openResult = false)
        )

        coVerify {
            recycledCoronaTestsProvider wasNot Called
            submissionRepository.testForType(BaseCoronaTest.Type.PCR)
        }
    }

    @Test
    fun `show duplicate warning if another personal test RAT is active`() = runTest {
        every { submissionRepository.testForType(BaseCoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(anotherRAT)

        instance.restoreCoronaTest(
            recycledRAT,
            openResult = false
        ) shouldBe CoronaTestRestoreEvent.RestoreDuplicateTest(
            restoreRecycledTestRequest = recycledRAT.toRestoreRecycledTestRequest(openResult = false)
        )

        coVerify {
            recycledCoronaTestsProvider wasNot Called
            submissionRepository.testForType(BaseCoronaTest.Type.RAPID_ANTIGEN)
        }
    }
}
