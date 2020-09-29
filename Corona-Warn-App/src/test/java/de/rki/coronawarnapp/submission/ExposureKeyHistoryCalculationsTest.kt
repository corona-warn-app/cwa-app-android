package de.rki.coronawarnapp.submission

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ExposureKeyHistoryCalculationsTest {

    private lateinit var instance: ExposureKeyHistoryCalculations
    private lateinit var converter: KeyConverter
    private lateinit var todayMidnight: DateTime
    private lateinit var thisMorning: DateTime
    private lateinit var thisEvening: DateTime

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

        todayMidnight = DateTime(2012, 10, 15, 0, 0, DateTimeZone.UTC)
        thisMorning = DateTime(2012, 10, 15, 10, 0, DateTimeZone.UTC)
        thisEvening = DateTime(2012, 10, 15, 20, 0, DateTimeZone.UTC)
    }

    @Test
    fun test_limitKeyCount() {
        val tek1 = createKey(thisMorning)
        val tek2 = createKey(thisMorning.minusDays(14))
        val tek3 = createKey(thisMorning.minusDays(15))
        Assert.assertArrayEquals(
            arrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            instance.removeOldKeys(
                listOf(
                    tek1,
                    tek2,
                    tek3
                ),
                thisMorning
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
        // regular case
        var tek1 = createKey(thisMorning)
        var tek2 = createKey(thisMorning.minusDays(1))
        var result = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2)),
            intArrayOf(3998, 3999, 4000),
            thisMorning
        )
        Assert.assertArrayEquals(
            intArrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            result.map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(3998, 3999),
            result.map { it.daysSinceOnsetOfSymptoms }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(0, 1),
            result.map { it.transmissionRiskLevel }.toTypedArray().toIntArray()
        )

        // gap
        tek1 = createKey(thisEvening)
        tek2 = createKey(thisEvening.minusDays(7))
        result = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)),
            intArrayOf(3998, 3999, 4000, 4001, 4002, 4003, 4004, 4005),
            thisEvening
        )
        Assert.assertArrayEquals(
            intArrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            result.map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(3998, 4005),
            result.map { it.daysSinceOnsetOfSymptoms }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(0, 7),
            result.map { it.transmissionRiskLevel }.toTypedArray().toIntArray()
        )

        // several keys in one day
        tek1 = createKey(todayMidnight)
        tek2 = createKey(todayMidnight)
        result = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)),
            intArrayOf(3998, 3999, 4000, 4001, 4002, 4003, 4004, 4005),
            todayMidnight
        )
        Assert.assertArrayEquals(
            intArrayOf(tek1.rollingStartIntervalNumber, tek1.rollingStartIntervalNumber),
            result.map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(3998, 3998),
            result.map { it.daysSinceOnsetOfSymptoms }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(0, 0),
            result.map { it.transmissionRiskLevel }.toTypedArray().toIntArray()
        )

        // submitting later that day
        tek1 = createKey(thisMorning)
        tek2 = createKey(thisEvening.minusDays(1))
        result = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2)),
            intArrayOf(3998, 3999, 4000),
            thisEvening
        )
        Assert.assertArrayEquals(
            intArrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            result.map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(3998, 3999),
            result.map { it.daysSinceOnsetOfSymptoms }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(0, 1),
            result.map { it.transmissionRiskLevel }.toTypedArray().toIntArray()
        )

        // several keys yesterday
        tek1 = createKey(thisMorning.minusDays(1))
        tek2 = createKey(thisEvening.minusDays(1))
        val tek3 = createKey(thisMorning)
        result = instance.toExternalFormat(
            listOf(tek3, tek2, tek1),
            TransmissionRiskVector(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)),
            intArrayOf(3998, 3999, 4000, 4001, 4002, 4003, 4004, 4005),
            thisMorning
        )
        Assert.assertArrayEquals(
            intArrayOf(tek3.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber, tek1.rollingStartIntervalNumber),
            result.map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(3998, 3999, 3999),
            result.map { it.daysSinceOnsetOfSymptoms }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf(0, 1, 1),
            result.map { it.transmissionRiskLevel }.toTypedArray().toIntArray()
        )
    }

    private fun createKey(rollingStartIntervalNumber: Int) =
        TemporaryExposureKey.TemporaryExposureKeyBuilder()
            .setRollingStartIntervalNumber(rollingStartIntervalNumber).build()

    private fun createKey(dateTime: DateTime) =
        createKey((dateTime.millis / ExposureKeyHistoryCalculations.TEN_MINUTES_IN_MILLIS).toInt())
}
