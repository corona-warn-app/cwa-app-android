package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.migration.PCRTestMigration
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.storage.CoronaTestStorage
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRProcessor
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RapidAntigenProcessor
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import testhelpers.coroutines.runBlockingTest2

class CoronaTestRepositoryTest : BaseTest() {
    @MockK lateinit var storage: CoronaTestStorage
    @MockK lateinit var legacyMigration: PCRTestMigration
    @MockK lateinit var contactDiaryRepository: ContactDiaryRepository

    private var coronaTestsInStorage = mutableSetOf<CoronaTest>()

    private val pcrTest = PCRCoronaTest(
        identifier = "pcr-identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.PCR_REDEEMED,
    )
    @MockK lateinit var pcrProcessor: PCRProcessor

    @MockK lateinit var raProcessor: RapidAntigenProcessor
    private val raTest = RACoronaTest(
        identifier = "ra-identifier",
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.RAT_REDEEMED,
        testedAt = Instant.EPOCH,
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coronaTestsInStorage.add(pcrTest)
        coronaTestsInStorage.add(raTest)

        legacyMigration.apply {
            coEvery { startMigration() } returns emptySet()
            coEvery { finishMigration() } just Runs
        }

        storage.apply {
            every { coronaTests = any() } answers {
                coronaTestsInStorage.clear()
                coronaTestsInStorage.addAll(arg(0))
            }
            every { coronaTests } answers { coronaTestsInStorage }
        }

        contactDiaryRepository.apply {
            coEvery { updateTests(any()) } just Runs
        }

        pcrProcessor.apply {
            coEvery { updateSubmissionConsent(any(), any()) } answers { arg<PCRCoronaTest>(0) }
            coEvery { updateDccConsent(any(), any()) } answers { arg<PCRCoronaTest>(0) }
            every { type } returns CoronaTest.Type.PCR
        }

        raProcessor.apply {
            coEvery { updateSubmissionConsent(any(), any()) } answers { arg<RACoronaTest>(0) }
            coEvery { updateDccConsent(any(), any()) } answers { arg<RACoronaTest>(0) }
            every { type } returns CoronaTest.Type.RAPID_ANTIGEN
        }
    }

    private fun createInstance(scope: CoroutineScope) = CoronaTestRepository(
        appScope = scope,
        dispatcherProvider = TestDispatcherProvider(),
        storage = storage,
        processors = setOf(pcrProcessor, raProcessor),
        legacyMigration = legacyMigration,
        contactDiaryRepository = contactDiaryRepository,
    )

    @Test
    fun `give submission consent`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).updateSubmissionConsent(pcrTest.identifier, true)

        coVerify { pcrProcessor.updateSubmissionConsent(pcrTest, true) }
    }

    @Test
    fun `give dcc consent`() = runBlockingTest2(ignoreActive = true) {
        createInstance(this).updateDccConsent(raTest.identifier, true)

        coVerify { raProcessor.updateDccConsent(raTest, true) }
    }
}
