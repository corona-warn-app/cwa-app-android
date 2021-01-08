package de.rki.coronawarnapp.diagnosiskeys.storage

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyCacheDatabaseTest {
    private val database = KeyCacheDatabase.Factory(
        ApplicationProvider.getApplicationContext<Context>()
    ).create()
    private val dao = database.cachedKeyFiles()

    @Test
    fun crud() {
        val keyDay = CachedKeyInfo(
            type = CachedKeyInfo.Type.LOCATION_DAY,
            location = LocationCode("DE"),
            day = LocalDate.now(),
            hour = null,
            createdAt = Instant.now()
        )
        val keyHour = CachedKeyInfo(
            type = CachedKeyInfo.Type.LOCATION_HOUR,
            location = LocationCode("DE"),
            day = LocalDate.now(),
            hour = LocalTime.now(),
            createdAt = Instant.now()
        )
        runBlocking {
            dao.clear()

            dao.insertEntry(keyDay)
            dao.insertEntry(keyHour)
            dao.allEntries().first() shouldBe listOf(keyDay, keyHour)
            dao.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY.typeValue) shouldBe listOf(keyDay)
            dao.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR.typeValue) shouldBe listOf(keyHour)

            dao.updateDownloadState(keyDay.toDownloadUpdate("coffee"))
            dao.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY.typeValue).single().apply {
                isDownloadComplete shouldBe true
                etag shouldBe "coffee"
            }
            dao.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR.typeValue).single().apply {
                isDownloadComplete shouldBe false
                etag shouldBe null
            }

            dao.updateDownloadState(keyHour.toDownloadUpdate("with milk"))
            dao.getEntriesForType(CachedKeyInfo.Type.LOCATION_DAY.typeValue).single().apply {
                isDownloadComplete shouldBe true
                etag shouldBe "coffee"
            }
            dao.getEntriesForType(CachedKeyInfo.Type.LOCATION_HOUR.typeValue).single().apply {
                isDownloadComplete shouldBe true
                etag shouldBe "with milk"
            }

            dao.deleteEntry(keyDay)
            dao.allEntries().first() shouldBe listOf(
                keyHour.copy(
                    isDownloadComplete = true,
                    etag = "with milk"
                )
            )

            dao.clear()
            dao.allEntries().first() shouldBe emptyList<List<CachedKeyInfo>>()
        }
    }
}
