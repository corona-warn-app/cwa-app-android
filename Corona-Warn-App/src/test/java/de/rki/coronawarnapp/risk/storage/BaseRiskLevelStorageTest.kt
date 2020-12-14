package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testExposureWindow
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testExposureWindowDaoWrapper
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testRiskLevelResultDao
import de.rki.coronawarnapp.risk.storage.RiskStorageTestData.testRisklevelResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.ExposureWindowsDao
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.Factory
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase.RiskResultsDao
import de.rki.coronawarnapp.risk.storage.legacy.RiskLevelResultMigrator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BaseRiskLevelStorageTest : BaseTest() {

    @MockK lateinit var databaseFactory: Factory
    @MockK lateinit var database: RiskResultDatabase
    @MockK lateinit var riskResultTables: RiskResultsDao
    @MockK lateinit var exposureWindowTables: ExposureWindowsDao
    @MockK lateinit var riskLevelResultMigrator: RiskLevelResultMigrator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { databaseFactory.create() } returns database
        every { database.riskResults() } returns riskResultTables
        every { database.exposureWindows() } returns exposureWindowTables
        every { database.clearAllTables() } just Runs

        coEvery { riskLevelResultMigrator.getLegacyResults() } returns emptyList()

        every { riskResultTables.allEntries() } returns emptyFlow()
        coEvery { riskResultTables.insertEntry(any()) } just Runs
        coEvery { riskResultTables.deleteOldest(any()) } returns 7

        every { exposureWindowTables.allEntries() } returns emptyFlow()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance(
        storedResultLimit: Int = 10,
        onStoreExposureWindows: (String, RiskLevelResult) -> Unit = { id, result -> },
        onDeletedOrphanedExposureWindows: () -> Unit = {}
    ) = object : BaseRiskLevelStorage(
        riskResultDatabaseFactory = databaseFactory,
        riskLevelResultMigrator = riskLevelResultMigrator
    ) {
        override val storedResultLimit: Int = storedResultLimit

        override suspend fun storeExposureWindows(storedResultId: String, result: RiskLevelResult) {
            onStoreExposureWindows(storedResultId, result)
        }

        override suspend fun deletedOrphanedExposureWindows() {
            onDeletedOrphanedExposureWindows()
        }
    }

    @Test
    fun `exposureWindows are returned from database and mapped`() {
        val testDaoWrappers = flowOf(listOf(testExposureWindowDaoWrapper))
        every { exposureWindowTables.allEntries() } returns testDaoWrappers

        runBlockingTest {
            val exposureWindowDAOWrappers = createInstance().exposureWindowsTables.allEntries()
            exposureWindowDAOWrappers shouldBe testDaoWrappers
            exposureWindowDAOWrappers.first().map { it.toExposureWindow() } shouldBe listOf(testExposureWindow)
        }
    }

    @Test
    fun `riskLevelResults are returned from database and mapped`() {
        every { riskResultTables.allEntries() } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.allEntries() } returns flowOf(emptyList())

        runBlockingTest {
            val instance = createInstance()
            instance.riskLevelResults.first() shouldBe listOf(testRisklevelResult)

            verify { riskLevelResultMigrator wasNot Called }
        }
    }

    @Test
    fun `riskLevelResults with exposure windows are returned from database and mapped`() {
        every { riskResultTables.allEntries() } returns flowOf(listOf(testRiskLevelResultDao))
        every { exposureWindowTables.allEntries() } returns flowOf(listOf(testExposureWindowDaoWrapper))

        runBlockingTest {
            val instance = createInstance()
            val riskLevelResult = testRisklevelResult.copy(exposureWindows = listOf(testExposureWindow))
            instance.riskLevelResults.first() shouldBe listOf(riskLevelResult)

            verify { riskLevelResultMigrator wasNot Called }
        }
    }

    @Test
    fun `if no risk level results are available we try to get legacy results`() {
        coEvery { riskLevelResultMigrator.getLegacyResults() } returns listOf(mockk(), mockk())
        every { riskResultTables.allEntries() } returns flowOf(emptyList())
        every { exposureWindowTables.allEntries() } returns flowOf(emptyList())

        runBlockingTest {
            val instance = createInstance()
            instance.riskLevelResults.first().size shouldBe 2

            coVerify { riskLevelResultMigrator.getLegacyResults() }
        }
    }

    @Test
    fun `errors when storing risklevel result are rethrown`() = runBlockingTest {
        coEvery { riskResultTables.insertEntry(any()) } throws IllegalStateException("No body expects the...")
        val instance = createInstance()
        shouldThrow<java.lang.IllegalStateException> {
            instance.storeResult(testRisklevelResult)
        }
    }

    @Test
    fun `errors when storing exposure window results are thrown`() = runBlockingTest {
        val instance = createInstance(onStoreExposureWindows = { _, _ -> throw IllegalStateException("Surprise!") })
        shouldThrow<IllegalStateException> {
            instance.storeResult(testRisklevelResult)
        }
    }

    @Test
    fun `storeResult works`() = runBlockingTest {
        val mockStoreWindows: (String, RiskLevelResult) -> Unit = spyk()
        val mockDeleteOrphanedWindows: () -> Unit = spyk()

        val instance = createInstance(
            onStoreExposureWindows = mockStoreWindows,
            onDeletedOrphanedExposureWindows = mockDeleteOrphanedWindows
        )
        instance.storeResult(testRisklevelResult)

        coVerify {
            riskResultTables.insertEntry(any())
            riskResultTables.deleteOldest(instance.storedResultLimit)
            mockStoreWindows.invoke(any(), testRisklevelResult)
            mockDeleteOrphanedWindows.invoke()
        }
    }

    @Test
    fun `clear works`() = runBlockingTest {
        createInstance().clear()
        verify { database.clearAllTables() }
    }
}
