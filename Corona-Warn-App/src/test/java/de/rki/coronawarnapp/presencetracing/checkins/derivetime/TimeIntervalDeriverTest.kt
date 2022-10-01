package de.rki.coronawarnapp.presencetracing.checkins.derivetime

import de.rki.coronawarnapp.appconfig.PresenceTracingSubmissionParamContainer
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.DurationFilter
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingSubmissionParameters.AerosoleDecayFunctionLinear
import de.rki.coronawarnapp.server.protocols.internal.v2.RiskCalculationParametersOuterClass.Range
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Test scenarios reference: [https://github.com/corona-warn-app/cwa-app-tech-spec/blob/
 * proposal/event-registration-mvp/test-cases/pt-derive-time-interval-data.json]
 */
internal class TimeIntervalDeriverTest : BaseTest() {

    /* "defaultConfiguration": {
    "durationFilters": [
      {
        "dropIfDurationInRange": {
          "min": 0,
          "max": 10,
          "maxExclusive": true
        }
      }
    ],
    "aerosoleDecayTime": [
      {
        "durationRange": {
          "min": 0,
          "max": 30
        },
        "slope": 1,
        "intercept": 0
      },
      {
        "durationRange": {
          "min": 30,
          "max": 9999,
          "minExclusive": true
        },
        "slope": 0,
        "intercept": 30
      }
    ]
  } */

    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm zzzz")
        .withZone(ZoneId.of("Europe/Berlin"))

    private val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm Z")
        .withZone(ZoneId.of("Europe/Berlin"))

    private val presenceTracingConfig = PresenceTracingSubmissionParamContainer(
        durationFilters = listOf(
            DurationFilter.newBuilder()
                .setDropIfMinutesInRange(
                    Range.newBuilder()
                        .setMin(0.0)
                        .setMax(10.0)
                        .setMaxExclusive(true)
                        .build()
                )
                .build()
        ),
        aerosoleDecayLinearFunctions = listOf(
            AerosoleDecayFunctionLinear.newBuilder()
                .setMinutesRange(
                    Range.newBuilder()
                        .setMin(0.0)
                        .setMax(30.0)
                        .build()
                )
                .setSlope(1.0)
                .setIntercept(0.0)
                .build(),
            AerosoleDecayFunctionLinear.newBuilder()
                .setMinutesRange(
                    Range.newBuilder()
                        .setMin(30.0)
                        .setMax(9999.0)
                        .setMinExclusive(true)
                        .build()
                )
                .setSlope(0.0)
                .setIntercept(30.0)
                .build()
        )
    )

    @Test
    fun `Scenario 1`() {
        /*
        "description": "Scenario 1",
        "startDateStr": "2021-03-04 10:21+01:00",
        "endDateStr": "2021-03-04 10:29+01:00",
        "expStartDateStr": null,
        "expEndDateStr": null
        */
        presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:21 +01:00"),
            timeInSeconds("2021-03-04 10:29 +01:00")
        ) shouldBe null
    }

    @Test
    fun `Scenario 2`() {
        /*
         "description": "Scenario 2",
         "startDateStr": "2021-03-04 10:20+01:00",
         "endDateStr": "2021-03-04 10:30+01:00",
         "expStartDateStr": "2021-03-04 10:20+01:00",
         "expEndDateStr": "2021-03-04 10:40+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:20 +01:00"),
            timeInSeconds("2021-03-04 10:30 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:20 +0100"
        timeToString(endTime) shouldBe "2021-03-04 10:40 +0100"
    }

    @Test
    fun `Scenario 3`() {
        /*
         "description": "Scenario 3",
         "startDateStr": "2021-03-04 10:21+01:00",
         "endDateStr": "2021-03-04 10:31+01:00",
         "expStartDateStr": "2021-03-04 10:20+01:00",
         "expEndDateStr": "2021-03-04 10:40+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:21 +01:00"),
            timeInSeconds("2021-03-04 10:31 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:20 +0100"
        timeToString(endTime) shouldBe "2021-03-04 10:40 +0100"
    }

    @Test
    fun `Scenario 4`() {
        /*
         "description": "Scenario 4",
         "startDateStr": "2021-03-04 10:26+01:00",
         "endDateStr": "2021-03-04 10:36+01:00",
         "expStartDateStr": "2021-03-04 10:30+01:00",
         "expEndDateStr": "2021-03-04 10:50+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:26 +01:00"),
            timeInSeconds("2021-03-04 10:36 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:30 +0100"
        timeToString(endTime) shouldBe "2021-03-04 10:50 +0100"
    }

    @Test
    fun `Scenario 5`() {
        /*
          "description": "Scenario 5",
          "startDateStr": "2021-03-04 10:21+01:00",
          "endDateStr": "2021-03-04 10:33+01:00",
          "expStartDateStr": "2021-03-04 10:20+01:00",
          "expEndDateStr": "2021-03-04 10:40+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:21 +01:00"),
            timeInSeconds("2021-03-04 10:33 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:20 +0100"
        timeToString(endTime) shouldBe "2021-03-04 10:40 +0100"
    }

    @Test
    fun `Scenario 6`() {
        /*
          "description": "Scenario 6",
          "startDateStr": "2021-03-04 10:24+01:00",
          "endDateStr": "2021-03-04 10:36+01:00",
          "expStartDateStr": "2021-03-04 10:30+01:00",
          "expEndDateStr": "2021-03-04 10:50+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:24 +01:00"),
            timeInSeconds("2021-03-04 10:36 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:30 +0100"
        timeToString(endTime) shouldBe "2021-03-04 10:50 +0100"
    }

    @Test
    fun `Scenario 7`() {
        /*
          "description": "Scenario 7",
          "startDateStr": "2021-03-04 10:25+01:00",
          "endDateStr": "2021-03-04 10:39+01:00",
          "expStartDateStr": "2021-03-04 10:20+01:00",
          "expEndDateStr": "2021-03-04 10:50+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:25 +01:00"),
            timeInSeconds("2021-03-04 10:39 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:20 +0100"
        timeToString(endTime) shouldBe "2021-03-04 10:50 +0100"
    }

    @Test
    fun `Scenario 8`() {
        /*
          "description": "Scenario 8",
          "startDateStr": "2021-03-04 10:28+01:00",
          "endDateStr": "2021-03-04 10:42+01:00",
          "expStartDateStr": "2021-03-04 10:30+01:00",
          "expEndDateStr": "2021-03-04 11:00+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:28 +01:00"),
            timeInSeconds("2021-03-04 10:42 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:30 +0100"
        timeToString(endTime) shouldBe "2021-03-04 11:00 +0100"
    }

    @Test
    fun `Scenario 9`() {
        /*
          "description": "Scenario 9",
          "startDateStr": "2021-03-04 10:25+01:00",
          "endDateStr": "2021-03-04 10:40+01:00",
          "expStartDateStr": "2021-03-04 10:20+01:00",
          "expEndDateStr": "2021-03-04 10:50+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:25 +01:00"),
            timeInSeconds("2021-03-04 10:40 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:20 +0100"
        timeToString(endTime) shouldBe "2021-03-04 10:50 +0100"
    }

    @Test
    fun `Scenario 10`() {
        /*
          "description": "Scenario 10",
          "startDateStr": "2021-03-04 10:22+01:00",
          "endDateStr": "2021-03-04 11:12+01:00",
          "expStartDateStr": "2021-03-04 10:20+01:00",
          "expEndDateStr": "2021-03-04 11:40+01:00"
         */
        val (startTime, endTime) = presenceTracingConfig.deriveTime(
            timeInSeconds("2021-03-04 10:22 +01:00"),
            timeInSeconds("2021-03-04 11:12 +01:00")
        )!!

        timeToString(startTime) shouldBe "2021-03-04 10:20 +0100"
        timeToString(endTime) shouldBe "2021-03-04 11:40 +0100"
    }

    private fun timeInSeconds(dateTime: String): Long {
        return ZonedDateTime.parse(dateTime, timeFormat).toInstant().epochSecond
    }

    private fun timeToString(timeInSecond: Long): String {
        return Instant.ofEpochSecond(timeInSecond).atZone(ZoneOffset.UTC).format(format)
    }
}
