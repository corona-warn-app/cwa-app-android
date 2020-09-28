package de.rki.coronawarnapp.submission

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.joda.time.DateTime
import org.joda.time.Instant
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ExposureKeyHistoryCalculationsTest {

    private lateinit var instance: ExposureKeyHistoryCalculations
    private lateinit var converter: KeyConverter

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
    }

    @Test
    fun test_limitKeyCount() {
        val tek1 = createKey(2012, 10, 15)
        val tek2 = createKey(2012, 10, 1)
        Assert.assertArrayEquals(
            arrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            instance.removeOldKeys(
                listOf(
                    tek1,
                    tek2,
                    createKey(2012, 9, 30)
                ),
                DateTime(2012, 10, 15, 0, 0).toInstant()
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
        val tek1 = createKey(2012, 10, 2)
        val tek2 = createKey(2012, 10, 1)
        val toExternalFormat = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2)),
            intArrayOf(3998, 3999, 4000),
            DateTime(2012, 10, 2, 1, 1).toInstant()
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

    @Test
    fun test_daysAgo() {
        Assert.assertEquals(
            0, instance.ageInDays(
                DateTime(2012, 3, 4, 1, 2).toInstant(),
                DateTime(2012, 3, 4, 16, 9).toInstant()
            )
        )
        Assert.assertEquals(
            2, instance.ageInDays(
                DateTime(2013, 12, 31, 1, 2).toInstant(),
                DateTime(2014, 1, 2, 16, 9).toInstant()
            )
        )
    }

    private fun createKey(rollingStartIntervalNumber: Int) =
        TemporaryExposureKey.TemporaryExposureKeyBuilder()
            .setRollingStartIntervalNumber(rollingStartIntervalNumber).build()

    private fun createKey(instant: Instant) =
        createKey((instant.millis / ExposureKeyHistoryCalculations.TEN_MINUTES_IN_MILLIS).toInt())

    private fun createKey(year: Int, month: Int, day: Int) =
        createKey(DateTime(year, month, day, 0, 0).toInstant())
}
