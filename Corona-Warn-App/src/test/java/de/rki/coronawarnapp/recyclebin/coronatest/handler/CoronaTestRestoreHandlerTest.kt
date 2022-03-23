package de.rki.coronawarnapp.recyclebin.coronatest.handler

import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.RegistrationToken
import de.rki.coronawarnapp.coronatest.type.TestIdentifier
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
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
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
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

    private val familyCoronaTest = object : FamilyCoronaTest {
        override val identifier: TestIdentifier = "family-pcr-identifier"
        override val personName: String = "personName"
        override val type: CoronaTest.Type = CoronaTest.Type.PCR
        override val registeredAt: Instant = Instant.EPOCH
        override val registrationToken: RegistrationToken = "registrationToken"
        override val testResultReceivedAt: Instant = Instant.EPOCH
        override val testResult: CoronaTestResult = CoronaTestResult.PCR_POSITIVE
        override val isRedeemed: Boolean = true
        override val isPositive: Boolean = true
        override val isNegative: Boolean = false
        override val isPending: Boolean = false
        override val labId: String = "labId"
        override val isViewed: Boolean = true
        override val didShowBadge: Boolean = false
        override val isResultAvailableNotificationSent: Boolean = true
        override val isDccSupportedByPoc: Boolean = true
        override val isDccConsentGiven: Boolean = true
        override val isDccDataSetCreated: Boolean = true
        override val qrCodeHash: String = "qrCodeHash"
        override val recycledAt: Instant = Instant.EPOCH
    }

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { submissionRepository.testForType(any()) } returns flowOf(null)
    }

    @Test
    fun `restore personal corona test PCR whether no other PCR is active`() = runBlockingTest {
        instance.restoreCoronaTest(recycledPCR) shouldBe CoronaTestRestoreEvent.RestoredTest

        coVerify {
            recycledCoronaTestsProvider.restoreCoronaTest(recycledPCR.identifier)
            submissionRepository.testForType(CoronaTest.Type.PCR)
        }
    }

    @Test
    fun `restore personal corona test RAT whether no other RAT is active`() = runBlockingTest {
        instance.restoreCoronaTest(recycledRAT) shouldBe CoronaTestRestoreEvent.RestoredTest

        coVerify {
            recycledCoronaTestsProvider.restoreCoronaTest(recycledRAT.identifier)
            submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN)
        }
    }

    @Test
    fun `restore family corona test PCR regardless of whether another PCR is active`() = runBlockingTest {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(recycledPCR)

        instance.restoreCoronaTest(familyCoronaTest) shouldBe CoronaTestRestoreEvent.RestoredTest

        coVerify {
            recycledCoronaTestsProvider.restoreCoronaTest(familyCoronaTest.identifier)
            submissionRepository wasNot Called
        }
    }

    @Test
    fun `show duplicate warning if another personal test PCR is active`() = runBlockingTest {
        every { submissionRepository.testForType(CoronaTest.Type.PCR) } returns flowOf(anotherPCR)

        instance.restoreCoronaTest(recycledPCR) shouldBe CoronaTestRestoreEvent.RestoreDuplicateTest(
            restoreRecycledTestRequest = recycledPCR.toRestoreRecycledTestRequest()
        )

        coVerify {
            recycledCoronaTestsProvider wasNot Called
            submissionRepository.testForType(CoronaTest.Type.PCR)
        }
    }

    @Test
    fun `show duplicate warning if another personal test RAT is active`() = runBlockingTest {
        every { submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN) } returns flowOf(anotherRAT)

        instance.restoreCoronaTest(recycledRAT) shouldBe CoronaTestRestoreEvent.RestoreDuplicateTest(
            restoreRecycledTestRequest = recycledRAT.toRestoreRecycledTestRequest()
        )

        coVerify {
            recycledCoronaTestsProvider wasNot Called
            submissionRepository.testForType(CoronaTest.Type.RAPID_ANTIGEN)
        }
    }
}
