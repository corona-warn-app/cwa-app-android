package de.rki.coronawarnapp.submission.data.tekhistory.storage.internal

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.data.tekhistory.internal.TEKEntryDao
import de.rki.coronawarnapp.submission.data.tekhistory.internal.TEKHistoryDatabase
import de.rki.coronawarnapp.submission.data.tekhistory.internal.toPersistedTEK
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.room.mockDefaultOperations

class TEKHistoryStorageTest : BaseTest() {

    @MockK lateinit var tekHistoryDatabaseFactory: TEKHistoryDatabase.Factory
    @MockK lateinit var tekHistoryDatabase: TEKHistoryDatabase
    @MockK lateinit var tekHistoryTables: TEKHistoryDatabase.TEKHistoryDao

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { tekHistoryDatabaseFactory.create() } returns tekHistoryDatabase
        every { tekHistoryDatabase.tekHistory() } returns tekHistoryTables

        coEvery { tekHistoryTables.insertEntry(any()) } just Runs
        every { tekHistoryDatabase.clearAllTables() } just Runs

        tekHistoryDatabase.mockDefaultOperations()
    }

    private fun createInstance() = TEKHistoryStorage(
        tekHistoryDatabaseFactory = tekHistoryDatabaseFactory
    )

    private val testData = TEKHistoryStorage.TEKBatch(
        obtainedAt = Instant.ofEpochMilli(1234),
        batchId = "batch-id",
        keys = listOf(
            TemporaryExposureKey.TemporaryExposureKeyBuilder().apply {
                setKeyData("keydata".toByteArray())
                setRollingStartIntervalNumber(123)
                setTransmissionRiskLevel(3)
                setRollingPeriod(144)
                setReportType(1)
                setDaysSinceOnsetOfSymptoms(7)
            }.build(),
            TemporaryExposureKey.TemporaryExposureKeyBuilder().apply {
                setKeyData("keydata2".toByteArray())
                setRollingStartIntervalNumber(123)
                setTransmissionRiskLevel(3)
                setRollingPeriod(144)
                setReportType(1)
                setDaysSinceOnsetOfSymptoms(8)
            }.build()
        )
    )
    private val persistedTEK1 = TEKEntryDao(
        id = testData.keys[0].keyData.toByteString().base64(),
        batchId = testData.batchId,
        obtainedAt = testData.obtainedAt,
        persistedTEK = testData.keys[0].toPersistedTEK()
    )

    private val persistedTEK2 = TEKEntryDao(
        id = testData.keys[1].keyData.toByteString().base64(),
        batchId = testData.batchId,
        obtainedAt = testData.obtainedAt,
        persistedTEK = testData.keys[1].toPersistedTEK()
    )

    @Test
    fun `store data`() = runBlockingTest {
        val instance = createInstance()

        instance.storeTEKData(testData)
        coVerifySequence {
            tekHistoryTables.insertEntry(persistedTEK1)
            tekHistoryTables.insertEntry(persistedTEK2)
        }
    }

    @Test
    fun `retrieve data`() = runBlockingTest {
        every { tekHistoryTables.allEntries() } returns flowOf(listOf(persistedTEK1, persistedTEK2))
        val instance = createInstance()
        instance.tekData.first() shouldBe listOf(testData)
    }

    @Test
    fun `clear all data`() = runBlockingTest {
        createInstance().clear()
        coVerifySequence { tekHistoryDatabase.clearAllTables() }
    }
}
