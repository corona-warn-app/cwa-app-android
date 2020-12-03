package de.rki.coronawarnapp.submission.data.tekhistory.storage.internal

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryStorage
import de.rki.coronawarnapp.submission.data.tekhistory.internal.TEKEntryDao
import de.rki.coronawarnapp.submission.data.tekhistory.internal.toPersistedTEK
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.toByteString
import org.joda.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TEKEntryDaoTest : BaseTest() {

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
            }.build()
        )
    )

    private val testDataDao = TEKEntryDao(
        id = "keydata".toByteArray().toByteString().base64(),
        obtainedAt = Instant.ofEpochMilli(1234),
        batchId = "batch-id",
        persistedTEK = TEKEntryDao.PersistedTEK(
            keyData = "keydata".toByteArray(),
            rollingStartIntervalNumber = 123,
            transmissionRiskLevel = 3,
            rollingPeriod = 144,
            reportType = 1,
            daysSinceOnsetOfSymptoms = 7
        )
    )

    @Test
    fun `temporary exposure key to persisted tek`() {
        testData.keys[0].toPersistedTEK() shouldBe testDataDao.persistedTEK
    }

    @Test
    fun `persisted tek to temporary exposure key`() {
        testDataDao.persistedTEK.toTemporaryExposureKey() shouldBe testData.keys.first()
    }
}
