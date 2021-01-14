package de.rki.coronawarnapp.submission.task

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.util.TimeStamper
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

    private var timeStamper = TimeStamper()

    @Before
    fun setUp() {
        todayMidnight = DateTime(2012, 10, 15, 0, 0, DateTimeZone.UTC)
        thisMorning = DateTime(2012, 10, 15, 10, 0, DateTimeZone.UTC)
        thisEvening = DateTime(2012, 10, 15, 20, 0, DateTimeZone.UTC)

        converter = object : KeyConverter {
            override fun toExternalFormat(
                key: TemporaryExposureKey,
                riskValue: Int,
                daysSinceOnsetOfSymptoms: Int
            ) =
                TemporaryExposureKeyExportOuterClass.TemporaryExposureKey.newBuilder()
                    .setRollingStartIntervalNumber(key.rollingStartIntervalNumber)
                    .setTransmissionRiskLevel(riskValue)
                    .setDaysSinceOnsetOfSymptoms(daysSinceOnsetOfSymptoms)
                    .build()
        }

        instance = ExposureKeyHistoryCalculations(
            TransmissionRiskVectorDeterminator(timeStamper),
            DaysSinceOnsetOfSymptomsVectorDeterminator(timeStamper),
            converter,
            timeStamper
        )
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
                thisMorning.toLocalDate()
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
            thisMorning.toLocalDate()
        )
        f(
            result,
            intArrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            intArrayOf(3998, 3999),
            intArrayOf(0, 1)
        )

        // gap
        tek1 = createKey(thisEvening)
        tek2 = createKey(thisEvening.minusDays(7))
        result = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)),
            intArrayOf(3998, 3999, 4000, 4001, 4002, 4003, 4004, 4005),
            thisMorning.toLocalDate()
        )
        f(
            result,
            intArrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            intArrayOf(3998, 4005),
            intArrayOf(0, 7)
        )

        // several keys in one day
        tek1 = createKey(todayMidnight)
        tek2 = createKey(todayMidnight)
        result = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)),
            intArrayOf(3998, 3999, 4000, 4001, 4002, 4003, 4004, 4005),
            thisMorning.toLocalDate()
        )
        f(
            result,
            intArrayOf(tek1.rollingStartIntervalNumber, tek1.rollingStartIntervalNumber),
            intArrayOf(3998, 3998),
            intArrayOf(0, 0)
        )

        // submitting later that day
        tek1 = createKey(thisMorning)
        tek2 = createKey(thisEvening.minusDays(1))
        result = instance.toExternalFormat(
            listOf(tek1, tek2),
            TransmissionRiskVector(intArrayOf(0, 1, 2)),
            intArrayOf(3998, 3999, 4000),
            thisEvening.toLocalDate()
        )
        f(
            result,
            intArrayOf(tek1.rollingStartIntervalNumber, tek2.rollingStartIntervalNumber),
            intArrayOf(3998, 3999),
            intArrayOf(0, 1)
        )

        // several keys yesterday
        tek1 = createKey(thisMorning.minusDays(1))
        tek2 = createKey(thisEvening.minusDays(1))
        val tek3 = createKey(thisMorning)
        result = instance.toExternalFormat(
            listOf(tek3, tek2, tek1),
            TransmissionRiskVector(intArrayOf(0, 1, 2, 3, 4, 5, 6, 7)),
            intArrayOf(3998, 3999, 4000, 4001, 4002, 4003, 4004, 4005),
            thisMorning.toLocalDate()
        )
        f(
            result, intArrayOf(
                tek3.rollingStartIntervalNumber,
                tek2.rollingStartIntervalNumber,
                tek1.rollingStartIntervalNumber
            ), intArrayOf(3998, 3999, 3999), intArrayOf(0, 1, 1)
        )
    }

    private fun f(
        result: List<TemporaryExposureKeyExportOuterClass.TemporaryExposureKey>,
        intArrayOf: IntArray,
        intArrayOf1: IntArray,
        intArrayOf2: IntArray
    ) {
        Assert.assertArrayEquals(
            intArrayOf,
            result.map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf1,
            result.map { it.daysSinceOnsetOfSymptoms }.toTypedArray().toIntArray()
        )
        Assert.assertArrayEquals(
            intArrayOf2,
            result.map { it.transmissionRiskLevel }.toTypedArray().toIntArray()
        )
    }

    private fun createKey(rollingStartIntervalNumber: Int) =
        TemporaryExposureKey.TemporaryExposureKeyBuilder()
            .setRollingStartIntervalNumber(rollingStartIntervalNumber).build()

    private fun createKey(dateTime: DateTime) =
        createKey((dateTime.millis / ExposureKeyHistoryCalculations.TEN_MINUTES_IN_MILLIS).toInt())
}
