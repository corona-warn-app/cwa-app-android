package de.rki.coronawarnapp.deadman

import io.kotest.matchers.shouldBe
import org.joda.time.DateTime
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import org.junit.Test

class CalendarCalculationTest {

    private var pattern = "dd.MM.yyyy HH:mm"

    private val parsedTime: (String) -> DateTime = {input -> DateTime.parse(input, DateTimeFormat.forPattern(pattern))}

//    @MockK
//    lateinit var timeStamper: TimeStamper
//
//    @BeforeEach
//    fun setup() {
//        MockKAnnotations.init(this)
//        every { timeStamper.nowUTC } returns Instant.parse("2020-08-01T23:00:00.000Z")
//    }
//
//    @AfterEach
//    fun teardown() {
//        clearAllMocks()
//    }
//
//    private fun createTimeCalculator() = DeadmanNotificationTimeCalculation(
//        timeStamper = timeStamper
//    )
//
//    @Test
//    fun `multiple time test`() {
//        val currentTime = Instant.parse("2020-08-01T23:00:00.000Z")
//        every { timeStamper.nowUTC } returns currentTime
//
//        createTimeCalculator().getTime(Instant.parse("2020-08-01T22:00:00.000Z")) shouldBe 1
//    }

    @Test
    fun `multiple hours difference`() {

        // 72 hours passed -> deadman notification should be executed 36 hours ago
        DeadmanNotificationTimeCalculation().getHoursDiff(Instant.parse("2020-08-27T14:00:00.000Z"), Instant.parse("2020-08-30T14:00:00.000Z")) shouldBe -2160

        DeadmanNotificationTimeCalculation().getHoursDiff(Instant.parse("2020-08-27T14:00:00.000Z"), Instant.parse("2020-08-28T14:00:00.000Z")) shouldBe 720

        // Last success in future. Not possible case in real app
        DeadmanNotificationTimeCalculation().getHoursDiff(Instant.parse("2020-08-27T15:00:00.000Z"), Instant.parse("2020-08-27T14:00:00.000Z")) shouldBe 2220

    }
}
