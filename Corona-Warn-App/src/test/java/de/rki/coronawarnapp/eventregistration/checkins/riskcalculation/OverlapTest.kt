package de.rki.coronawarnapp.eventregistration.checkins.riskcalculation

import com.google.protobuf.ByteString.copyFromUtf8
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import okio.ByteString
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.Test

class OverlapTest {

    @Test
    fun `returns null if guids do not match`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T09:45+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        ) shouldBe null
    }

    @Test
    fun `returns null if check-in precedes warning`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T09:45+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        ) shouldBe null
    }

    @Test
    fun `returns null if check-in is preceded by warning`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T11:15+01:00",
            endDateStr = "2021-03-04T11:20+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        ) shouldBe null
    }

    @Test
    fun `returns null if check-in meets warning at the start`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T10:00+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        ) shouldBe null
    }

    @Test
    fun `returns null if check-in meets warning at the end`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T11:00+01:00",
            endDateStr = "2021-03-04T11:10+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        ) shouldBe null
    }

    @Test
    fun `returns overlap if check-in overlaps warning at the start`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        )!!.roundedMinutes shouldBe 12
    }

    @Test
    fun `returns overlap if check-in overlaps warning at the end`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:45+01:00",
            endDateStr = "2021-03-04T11:12+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        )!!.roundedMinutes shouldBe 15
    }

    @Test
    fun `returns overlap if check-in starts warning`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:00+01:00",
            endDateStr = "2021-03-04T10:13+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        )!!.roundedMinutes shouldBe 13
    }

    @Test
    fun `returns overlap if check-in during warning`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        )!!.roundedMinutes shouldBe 2
    }

    @Test
    fun `returns overlap if check-in finishes warning`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:30+01:00",
            endDateStr = "2021-03-04T11:00+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        )!!.roundedMinutes shouldBe 30
    }

    @Test
    fun `returns overlap if check-in equals warning`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:00+01:00",
            endDateStr = "2021-03-04T11:00+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        )!!.roundedMinutes shouldBe 60
    }

    @Test
    fun `returns overlap after rounding (up)`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:50+01:00",
            endDateStr = "2021-03-04T10:05:45+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        )!!.roundedMinutes shouldBe 6
    }

    @Test
    fun `returns overlap after rounding (down)`() {
        createCheckIn(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:50+01:00",
            endDateStr = "2021-03-04T10:05:15+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        )!!.roundedMinutes shouldBe 5
    }
}

fun createCheckIn(
    id: Long = 1L,
    traceLocationGuid: String,
    startDateStr: String,
    endDateStr: String
) = CheckIn(
    id = id,
    guid = traceLocationGuid,
    version = 1,
    type = 2,
    description = "My birthday party",
    address = "Malibu",
    traceLocationStart = Instant.parse(startDateStr),
    traceLocationEnd = null,
    defaultCheckInLengthInMinutes = null,
    traceLocationBytes = ByteString.EMPTY,
    signature = ByteString.EMPTY,
    checkInStart = Instant.parse(startDateStr),
    checkInEnd = Instant.parse(endDateStr),
    completed = false,
    createJournalEntry = false
)

fun createWarning(
    traceLocationGuid: String,
    startIntervalDateStr: String,
    period: Int,
    transmissionRiskLevel: Int
) = TraceWarning.TraceTimeIntervalWarning.newBuilder()
    .setLocationGuidHash(copyFromUtf8(traceLocationGuid.toSHA256()))
    .setPeriod(period)
    .setStartIntervalNumber((Duration(Instant.parse(startIntervalDateStr).millis).standardMinutes / 10).toInt())
    .setTransmissionRiskLevel(transmissionRiskLevel)
    .build()
