package de.rki.coronawarnapp.coronatest

import de.rki.coronawarnapp.contactdiary.storage.repo.ContactDiaryRepository
import de.rki.coronawarnapp.coronatest.errors.DuplicateCoronaTestException
import de.rki.coronawarnapp.coronatest.migration.PCRTestMigration
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.storage.CoronaTestStorage
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.coronatest.type.PersonalCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRCoronaTest
import de.rki.coronawarnapp.coronatest.type.pcr.PCRTestProcessor
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RATestProcessor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
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

    @MockK lateinit var pcrProcessor: PCRTestProcessor
    @MockK lateinit var raProcessor: RATestProcessor

    private var coronaTestsInStorage = mutableSetOf<PersonalCoronaTest>()

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

    private val raRegistrationRequest = CoronaTestQRCode.RapidAntigen(
        hash = "ra-hash",
        createdAt = Instant.EPOCH,
        rawQrCode = "rawQrCode"
    )
    private val raTest = RACoronaTest(
        identifier = raRegistrationRequest.identifier,
        lastUpdatedAt = Instant.EPOCH,
        registeredAt = Instant.EPOCH,
        registrationToken = "token",
        testResult = CoronaTestResult.RAT_REDEEMED,
        testedAt = Instant.EPOCH,
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

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
            coEvery { create(pcrRegistrationRequest) } returns pcrTest
            coEvery { updateSubmissionConsent(any(), any()) } answers { arg<PCRCoronaTest>(0) }
            every { type } returns BaseCoronaTest.Type.PCR
        }

        raProcessor.apply {
            coEvery { create(raRegistrationRequest) } returns raTest
            coEvery { updateSubmissionConsent(any(), any()) } answers { arg<RACoronaTest>(0) }
            every { type } returns BaseCoronaTest.Type.RAPID_ANTIGEN
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
        coronaTestsInStorage.add(pcrTest)

        createInstance(this).updateSubmissionConsent(pcrTest.identifier, true)

        coVerify { pcrProcessor.updateSubmissionConsent(pcrTest, true) }
    }

    @Test
    fun `test registration with default conditions`() = runBlockingTest2(ignoreActive = true) {
        coronaTestsInStorage.clear()
        val negativePcr = pcrTest.copy(testResult = CoronaTestResult.PCR_NEGATIVE)
        coEvery { pcrProcessor.create(pcrRegistrationRequest) } returns negativePcr
        val instance = createInstance(this)

        instance.registerTest(pcrRegistrationRequest) shouldBe negativePcr
    }

    @Test
    fun `test registration with default conditions and existing test`() = runBlockingTest2(ignoreActive = true) {
        coronaTestsInStorage.add(pcrTest)

        val instance = createInstance(this)

        shouldThrow<DuplicateCoronaTestException> {
            instance.registerTest(pcrRegistrationRequest) shouldBe pcrTest
        }
    }

    @Test
    fun `Filter corona tests by recycle state`() = runBlockingTest2(ignoreActive = true) {
        val recycledTest = pcrTest.copy(recycledAt = Instant.EPOCH)
        val notRecycledTest = raTest.copy(recycledAt = null)
        val tests = setOf(recycledTest, notRecycledTest)
        coronaTestsInStorage.apply {
            clear()
            addAll(tests)
        }

        createInstance(this).run {
            allCoronaTests.first() shouldBe tests
            coronaTests.first() shouldBe setOf(notRecycledTest)
            personalTestsInRecycleBin.first() shouldBe setOf(recycledTest)
        }
    }
}
