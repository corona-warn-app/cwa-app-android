package de.rki.coronawarnapp.deadman

import io.kotest.matchers.shouldBe
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test

class CalendarCalculationTest {

    private var pattern = "dd.MM.yyyy HH:mm"

    private val parsedTime: (String) -> DateTime = {input -> DateTime.parse(input, DateTimeFormat.forPattern(pattern))}

    @Test
    fun calculateCheck() {

        check("27.08.2020 14:00","30.08.2020 14:00", -2160, 0)
        check("27.08.2020 14:00","27.08.2020 15:00", 2100, 2100)
        check("27.08.2020 14:00","27.08.2020 14:00", 2160, 2160)
        check("27.08.2020 15:00","27.08.2020 14:00", 2220, 2160)

        // 72 hours passed -> deadman notification should be executed 36 hours ago
        DeadmanNotificationTimeCalculation().getTime(parsedTime("27.08.2020 14:00"), parsedTime("30.08.2020 14:00")) shouldBe -2160

        DeadmanNotificationTimeCalculation().getTime(parsedTime("27.08.2020 14:00"), parsedTime("28.08.2020 14:00")) shouldBe 720

        DeadmanNotificationTimeCalculation().getTime(parsedTime("27.08.2020 14:00"), parsedTime("27.08.2020 15:00")) shouldBe 2100

        // Edge case: zero hours left from last success risk calculation
        DeadmanNotificationTimeCalculation().getTime(parsedTime("27.08.2020 14:00"), parsedTime("27.08.2020 14:00")) shouldBe 2160

        // Last success in future. Not possible case in real app
        DeadmanNotificationTimeCalculation().getTime(parsedTime("27.08.2020 15:00"), parsedTime("27.08.2020 14:00")) shouldBe 2220

        DeadmanNotificationTimeCalculation().getTime(parsedTime("27.08.2020 15:00"), parsedTime("27.08.2020 10:00")) shouldBe 2460

        DeadmanNotificationTimeCalculation().getTime(parsedTime("30.08.2020 15:00"), parsedTime("27.08.2020 14:00")) shouldBe 6540

    }

    private fun check(last: String, current: String, diffShouldBe: Int, delayShouldBe: Int) {
        val minutesDiff = DeadmanNotificationTimeCalculation().getTime(parsedTime(last), parsedTime(current))
        minutesDiff shouldBe diffShouldBe

        val delay = DeadmanNotificationTimeCalculation().normaliseInitialDelay(minutesDiff)
        delay shouldBe delayShouldBe
    }


}
