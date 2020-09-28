package de.rki.coronawarnapp.submission

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ExposureKeyHistoryCalculationsTest {

    private lateinit var instance: ExposureKeyHistoryCalculations
    private lateinit var converter: KeyConverter
    private lateinit var now: DateTime

    @Before
    fun setUp() {
        converter = object : KeyConverter {
            override fun toExternalFormat(
                key: TemporaryExposureKey,
                riskValue: Int,
                daysSinceOnsetOfSymptoms: Int
            ) =
                KeyExportFormat.TemporaryExposureKey.newBuilder()
                    .setRollingStartIntervalNumber(key.rollingStartIntervalNumber)
                    .setTransmissionRiskLevel(riskValue)
                    .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
                    .build()
        }

        instance = ExposureKeyHistoryCalculations(
            TransmissionRiskVectorDeterminator(),
            DaysSinceOnsetOfSymptomsVectorDeterminator(),
            converter
        )

        now = DateTime(2012, 10, 15, 0, 0)
    }

    @Test
    fun test_limitKeyCount() {
        val tek1 = createKey(now)
        val tek2 = createKey(now.minusDays(14))
        val tek3 = createKey(now.minusDays(15))
        Assert.assertArrayEquals(
            arrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            instance.removeOldKeys(
                listOf(
                    tek1,
                    tek2,
                    tek3
                ),
                now
            ).map { it.rollingStartIntervalNumber }.toTypedArray()
        )
    }

    @Test
    fun test_toSortedHistory() {
        Assert.assertArrayEquals(
            intArrayOf(8, 4, 3, 2, 1), instance.sortWithRecentKeyFirst(
                listOf(
                    createKey(3),
                    createKey(8),
                    createKey(1),
                    createKey(2),
                    createKey(4)
                )
            ).map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
    }

    @Test
    fun test_toExternalFormat() {
        val tek1 = createKey(now)
        val tek2 = createKey(now.minusDays(1))
        val toExternalFormat = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2)),
            intArrayOf(3998, 3999, 4000),
            now
        )
        Assert.assertArrayEquals(
            intArrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            toExternalFormat.map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(3998, 3999),
            toExternalFormat.map { it.daysSinceOnsetOfSymptoms }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(0, 1),
            toExternalFormat.map { it.transmissionRiskLevel }.toTypedArray().toIntArray()
        )
    }

    private fun createKey(rollingStartIntervalNumber: Int) =
        TemporaryExposureKey.TemporaryExposureKeyBuilder()
            .setRollingStartIntervalNumber(rollingStartIntervalNumber).build()

    private fun createKey(dateTime: DateTime) =
        createKey((dateTime.millis / ExposureKeyHistoryCalculations.TEN_MINUTES_IN_MILLIS).toInt())
}
