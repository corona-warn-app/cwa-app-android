package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

class FindMatchesTest {

    @Test
    fun `findMatches works`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationGuid = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:45+01:00"
        )

        val warning1 = createWarning(
            traceLocationGuid = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:30+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startIntervalDateStr = "2021-03-04T15:30+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )
        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }
            override val warningPackageId: Long
                get() = 1L
        }
        runBlockingTest {
            val result = findMatches(listOf(checkIn1, checkIn2), warningPackage)
            result.size shouldBe 1
            result[0].checkInId shouldBe 3L
            result[0].roundedMinutes shouldBe 15
        }
    }
}
