package de.rki.coronawarnapp.diagnosiskeys.storage

import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class CachedKeyFileTest : BaseTest() {
    private val type = CachedKeyInfo.Type.LOCATION_DAY
    private val location = LocationCode("DE")
    private val day = LocalDate.parse("2222-12-31")
    private val hour = LocalTime.parse("23:59:00.000")
    private val now = Instant.EPOCH

    @Test
    fun `secondary constructor`() {
        val key = CachedKeyInfo(type, location, day, hour, now)

        key.id shouldBe CachedKeyInfo.calculateId(location, day, hour, type)
        key.etag shouldBe null
        key.isDownloadComplete shouldBe false
    }

    @Test
    fun `keyfile id calculation`() {
        val calculatedId1 = CachedKeyInfo.calculateId(location, day, hour, type)
        val calculatedId2 = CachedKeyInfo.calculateId(location, day, hour, type)
        calculatedId1 shouldBe calculatedId2

        calculatedId1 shouldBe "550b64773e052b9ddf232998a92846833ed3f907"
    }

    @Test
    fun `to completion`() {
        val key = CachedKeyInfo(type, location, day, hour, now)
        val testChecksum = "testchecksum"
        val downloadCompleteUpdate = key.toDownloadUpdate(testChecksum)

        downloadCompleteUpdate shouldBe CachedKeyInfo.DownloadUpdate(
            id = downloadCompleteUpdate.id,
            isDownloadComplete = true,
            etag = testChecksum
        )
    }

    @Test
    fun `trip changed typing`() {
        CachedKeyInfo.Type.LOCATION_DAY.typeValue shouldBe "country_day"
        CachedKeyInfo.Type.LOCATION_HOUR.typeValue shouldBe "country_hour"
    }
}
