package de.rki.coronawarnapp.risk.storage

import de.rki.coronawarnapp.risk.RiskLevelResult
import de.rki.coronawarnapp.risk.storage.internal.RiskResultDatabase
import de.rki.coronawarnapp.risk.storage.legacy.RiskLevelResultMigrator
import de.rki.coronawarnapp.storage.LocalData
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BaseRiskLevelStorageTest : BaseTest() {

    @MockK lateinit var riskResultDatabaseFactory: RiskResultDatabase.Factory
    @MockK lateinit var riskLevelResultMigrator: RiskLevelResultMigrator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(LocalData)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance(
        storedResultLimit: Int = 10,
        onStoreExposureWindows: (String, RiskLevelResult) -> Unit,
        onDeletedOrphanedExposureWindows: () -> Unit
    ) = object : BaseRiskLevelStorage(
        riskResultDatabaseFactory = riskResultDatabaseFactory,
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
        TODO()
    }

    @Test
    fun `riskLevelResults are returned from database and mapped`() {
        TODO()
    }

    @Test
    fun `if no risk level results are available we try to get legacy results`() {
        TODO()
    }

    @Test
    fun `errors when storing risklevel result are rethrown`() {
        TODO()
    }

    @Test
    fun `errors when storing exposure window results are thrown`() {
        TODO()
    }

    @Test
    fun `old risklevel results are cleaned up depending on configure limit`() {
        TODO()
    }

    @Test
    fun `clear clears all tables`() {
        TODO()
    }

    @Test
    fun `storing results stores the result and the exposure windows if available`() {
        TODO()
    }

    @Test
    fun `storeResult works`() {
        TODO()
    }

    @Test
    fun `clear works`() {
        TODO()
    }
}
