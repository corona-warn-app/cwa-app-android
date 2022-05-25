package de.rki.coronawarnapp.bugreporting.debuglog.upload.history.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.migrations.SharedPreferencesMigration
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.LogUpload
import de.rki.coronawarnapp.bugreporting.debuglog.upload.history.model.UploadHistory
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.preferences.MockSharedPreferences
import java.time.Instant

class UploadHistoryStorageModuleTest : BaseTest() {

    @MockK lateinit var context: Context

    private lateinit var migration: SharedPreferencesMigration<UploadHistory>
    private lateinit var sharedPrefs: SharedPreferences
    private val gson = SerializationModule().baseGson()

    private val defaultUploadHistory = UploadHistory()
    private val testUploadHistory = UploadHistory(
        logs = listOf(
            LogUpload(id = "id1", uploadedAt = Instant.parse("2021-02-01T15:00:00.000Z")),
            LogUpload(id = "id2", uploadedAt = Instant.parse("2021-02-02T15:00:00.000Z"))
        )
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        sharedPrefs = MockSharedPreferences()
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs

        migration = UploadHistoryStorageModule.provideMigration(context, gson)
    }

    @Test
    fun `migration success`() = runTest {
        val testUploadHistoryJson = """
            {
              "logs": [
                {
                  "id": "id1",
                  "uploadedAt": 1612191600000
                },
                {
                  "id": "id2",
                  "uploadedAt": 1612278000000
                }
              ]
            }
        """.trimIndent()

        sharedPrefs.edit(true) {
            putString(LEGACY_UPLOAD_HISTORY_KEY, testUploadHistoryJson)
        }

        val migratedHistory = migration.migrate(defaultUploadHistory)
        migratedHistory shouldBe testUploadHistory
    }

    @Test
    fun `returns default if migration fails`() = runTest {
        sharedPrefs.edit(true) {
            putString(LEGACY_UPLOAD_HISTORY_KEY, "Invalid Data")
        }

        val migratedHistory = migration.migrate(defaultUploadHistory)
        migratedHistory shouldBe defaultUploadHistory
    }
}
