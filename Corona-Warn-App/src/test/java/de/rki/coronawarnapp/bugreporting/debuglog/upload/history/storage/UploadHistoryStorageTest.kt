package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.UploadHistory
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.FakeTypedDataStore
import java.time.Instant

class UploadHistoryStorageTest : BaseTest() {

    private val defaultUploadHistory = UploadHistory()

    private val dataStore = FakeTypedDataStore(defaultValue = defaultUploadHistory, shouldLog = true)
    private val uploadHistoryStorage = UploadHistoryStorage(dataStore)

    private val testLogs = listOf(
        LogUpload(id = "id1", uploadedAt = Instant.parse("2021-02-01T15:00:00.000Z")),
        LogUpload(id = "id2", uploadedAt = Instant.parse("2021-02-02T15:00:00.000Z"))
    )

    private val testUploadHistory = UploadHistory(logs = testLogs)

    @AfterEach
    fun cleanup() {
        dataStore.reset()
    }

    @Test
    fun `upload history is empty by default`() = runTest {
        uploadHistoryStorage.uploadHistory.first() shouldBe defaultUploadHistory
    }

    @Test
    fun `upload history is loaded from DataStore`() = runTest {
        dataStore.updateData { testUploadHistory }

        uploadHistoryStorage.uploadHistory.first() shouldBe testUploadHistory
    }

    @Test
    fun `upload history save and load`() = runTest {
        uploadHistoryStorage.update { it.copy(logs = testLogs) }

        uploadHistoryStorage.uploadHistory.first() shouldBe testUploadHistory
        dataStore.data.first() shouldBe testUploadHistory
    }

    @Test
    fun `upload history reset`() = runTest {
        uploadHistoryStorage.update { it.copy(logs = testLogs) }
        uploadHistoryStorage.uploadHistory.first() shouldBe testUploadHistory
        dataStore.data.first() shouldBe testUploadHistory

        uploadHistoryStorage.reset()
        uploadHistoryStorage.uploadHistory.first() shouldBe defaultUploadHistory
        dataStore.data.first() shouldBe defaultUploadHistory
    }
}
