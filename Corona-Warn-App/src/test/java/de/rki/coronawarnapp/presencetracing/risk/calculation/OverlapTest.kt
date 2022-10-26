package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.toOkioByteString
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeHex
import okio.ByteString.Companion.encode
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.extensions.toInstant
import java.time.Duration
import java.time.temporal.ChronoUnit

class OverlapTest : BaseTest() {

    private val id = "id"

    private val locationId = "afa27b44d43b02a9fea41d13cedc2e4016cfcf87c5dbf990e593669aa8ce286d"
    private val locationIdHash = "0f37dac11d1b8118ea0b44303400faa5e3b876da9d758058b5ff7dc2e5da8230"

    @Test
    fun `test helper method createCheckIn`() {
        val checkIn = createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T09:45+01:00"
        )

        checkIn.traceLocationId shouldBe locationId.decodeHex()
        checkIn.traceLocationIdHash shouldBe locationIdHash.decodeHex()
    }

    @Test
    fun `test helper method createWarning`() {
        val warning = createWarning(
            traceLocationId = locationId,
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )
        warning.locationIdHash.toOkioByteString().hex() shouldBe locationIdHash
    }

    @Test
    fun `returns null if guids do not match`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T09:45+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        ) shouldBe null
    }

    @Test
    fun `returns null if check-in precedes warning`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T09:45+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        ) shouldBe null
    }

    @Test
    fun `returns null if check-in is preceded by warning`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T11:15+01:00",
            endDateStr = "2021-03-04T11:20+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        ) shouldBe null
    }

    @Test
    fun `returns null if check-in meets warning at the start`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T10:00+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        ) shouldBe null
    }

    @Test
    fun `returns null if check-in meets warning at the end`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T11:00+01:00",
            endDateStr = "2021-03-04T11:10+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        ) shouldBe null
    }

    @Test
    fun `returns overlap if check-in overlaps warning at the start`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T09:30+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        )!!.roundedMinutes shouldBe 12
    }

    @Test
    fun `returns overlap if check-in overlaps warning at the end`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T10:45+01:00",
            endDateStr = "2021-03-04T11:12+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        )!!.roundedMinutes shouldBe 15
    }

    @Test
    fun `returns overlap if check-in starts warning`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T10:00+01:00",
            endDateStr = "2021-03-04T10:13+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        )!!.roundedMinutes shouldBe 13
    }

    @Test
    fun `returns overlap if check-in during warning`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        )!!.roundedMinutes shouldBe 2
    }

    @Test
    fun `returns overlap if check-in finishes warning`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T10:30+01:00",
            endDateStr = "2021-03-04T11:00+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        )!!.roundedMinutes shouldBe 30
    }

    @Test
    fun `returns overlap if check-in equals warning`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T10:00+01:00",
            endDateStr = "2021-03-04T11:00+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        )!!.roundedMinutes shouldBe 60
    }

    @Test
    fun `returns overlap after rounding (up)`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T09:50+01:00",
            endDateStr = "2021-03-04T10:05:45+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        )!!.roundedMinutes shouldBe 6
    }

    @Test
    fun `returns overlap after rounding (down)`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T09:50+01:00",
            endDateStr = "2021-03-04T10:05:15+01:00"
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        )!!.roundedMinutes shouldBe 5
    }

    @Test
    fun `returns null if it matches our own ssubmitted CheckIn`() {
        createCheckIn(
            traceLocationId = locationId,
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00",
            isSubmitted = true,
        ).calculateOverlap(
            createWarning(
                traceLocationId = locationId,
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            ),
            traceWarningPackageId = id
        ) shouldBe null
    }
}

fun createCheckIn(
    id: Long = 1L,
    traceLocationId: String,
    startDateStr: String,
    endDateStr: String,
    isSubmitted: Boolean = false,
) = CheckIn(
    id = id,
    traceLocationId = traceLocationId.decodeHex(),
    version = 1,
    type = 2,
    description = "My birthday party",
    address = "Malibu",
    traceLocationStart = startDateStr.toInstant(),
    traceLocationEnd = null,
    defaultCheckInLengthInMinutes = null,
    cryptographicSeed = "cryptographicSeed".encode(),
    cnPublicKey = "cnPublicKey",
    checkInStart = startDateStr.toInstant(),
    checkInEnd = endDateStr.toInstant(),
    completed = false,
    createJournalEntry = false,
    isSubmitted = isSubmitted
)

fun createWarning(
    traceLocationId: String,
    startIntervalDateStr: String,
    period: Int,
    transmissionRiskLevel: Int
): TraceWarning.TraceTimeIntervalWarning = TraceWarning.TraceTimeIntervalWarning.newBuilder()
    .setLocationIdHash(traceLocationId.decodeHex().sha256().toProtoByteString())
    .setPeriod(period)
    .setStartIntervalNumber(
        (Duration.of(startIntervalDateStr.toInstant().toEpochMilli(), ChronoUnit.MILLIS).toMinutes() / 10).toInt()
    )
    .setTransmissionRiskLevel(transmissionRiskLevel)
    .build()
