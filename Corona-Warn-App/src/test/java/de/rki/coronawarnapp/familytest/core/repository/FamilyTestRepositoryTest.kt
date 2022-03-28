package de.rki.coronawarnapp.familytest.core.repository

import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestQRCode
import de.rki.coronawarnapp.coronatest.server.CoronaTestResult
import de.rki.coronawarnapp.coronatest.type.BaseCoronaTest
import de.rki.coronawarnapp.familytest.core.model.CoronaTest
import de.rki.coronawarnapp.familytest.core.model.FamilyCoronaTest
import de.rki.coronawarnapp.familytest.core.model.updateTestResult
import de.rki.coronawarnapp.familytest.core.storage.FamilyTestStorage
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class FamilyTestRepositoryTest : BaseTest() {

    @MockK lateinit var processor: CoronaTestProcessor
    @MockK lateinit var storage: FamilyTestStorage
    @MockK lateinit var timeStamper: TimeStamper

    private val nowUTC = Instant.parse("2021-03-15T05:45:00.000Z")

    private val qrCode = CoronaTestQRCode.PCR(
        qrCodeGUID = "guid",
        rawQrCode = "rawQrCode"
    )

    private val identifier = "identifier"
    private val test = CoronaTest(
        type = BaseCoronaTest.Type.PCR,
        identifier = identifier,
        registeredAt = nowUTC,
        registrationToken = "regtoken",
        testResult = CoronaTestResult.PCR_OR_RAT_PENDING
    )

    private val familyTest = FamilyCoronaTest(
        personName = "Maria",
        test
    )

    private val update = CoronaTestProcessor.CoronaTestResultUpdate(
        coronaTestResult = CoronaTestResult.PCR_POSITIVE
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns nowUTC
        coEvery { processor.register(qrCode) } returns test
        coEvery { processor.pollServer(test) } returns update
        coEvery { storage.familyTestMap } returns flowOf(mapOf(identifier to familyTest))
        coEvery { storage.familyTestRecycleBinMap } returns flowOf(mapOf())
        coEvery { storage.save(familyTest) } just Runs
        coEvery { storage.update(identifier, any()) } just Runs
        coEvery { storage.delete(familyTest) } just Runs
    }

    @Test
    fun `registration works`() = runBlockingTest {
        createInstance().registerTest(qrCode, "Maria") shouldBe familyTest

        coVerifySequence {
            storage.familyTestMap
            storage.familyTestRecycleBinMap
            processor.register(qrCode)
            storage.save(familyTest)
        }
    }

    @Test
    fun `refresh works`() = runBlockingTest {
        createInstance().refresh()
        coVerifySequence {
            storage.familyTestMap
            storage.familyTestRecycleBinMap
            processor.pollServer(test)
            storage.update(identifier, any())
        }
    }

    @Test
    fun `no polling for final results`() = runBlockingTest {
        coEvery { storage.familyTestMap } returns
            flowOf(mapOf(identifier to familyTest.updateTestResult(CoronaTestResult.PCR_POSITIVE)))
        createInstance().refresh()
        coVerify(exactly = 0) {
            processor.pollServer(test)
            storage.update(identifier, any())
        }
    }

    @Test
    fun `restoreTest calls update`() = runBlockingTest {
        val instance = createInstance()
        instance.restoreTest(identifier)
        coVerify {
            storage.update(identifier = identifier, any())
        }
    }

    @Test
    fun `moveTestToRecycleBin calls update`() = runBlockingTest {
        val instance = createInstance()
        instance.moveTestToRecycleBin(identifier)
        coVerify {
            storage.update(identifier = identifier, any())
        }
    }

    @Test
    fun `markViewed calls update`() = runBlockingTest {
        val instance = createInstance()
        instance.markViewed(identifier)
        coVerify {
            storage.update(identifier = identifier, any())
        }
    }

    @Test
    fun `updateResultNotification calls update`() = runBlockingTest {
        val instance = createInstance()
        instance.updateResultNotification(identifier, true)
        coVerify {
            storage.update(identifier = identifier, any())
        }
    }

    @Test
    fun `markBadgeAsViewed calls update`() = runBlockingTest {
        val instance = createInstance()
        instance.markBadgeAsViewed(identifier)
        coVerify {
            storage.update(identifier = identifier, any())
        }
    }

    @Test
    fun `markDccAsCreated calls update`() = runBlockingTest {
        val instance = createInstance()
        instance.markDccAsCreated(identifier, true)
        coVerify {
            storage.update(identifier = identifier, any())
        }
    }

    @Test
    fun `delete calls delete`() = runBlockingTest {
        val instance = createInstance()
        instance.deleteTest(identifier)
        coVerify {
            storage.delete(familyTest)
        }
    }

    private fun createInstance() = FamilyTestRepository(
        processor,
        storage,
        timeStamper
    )
}
