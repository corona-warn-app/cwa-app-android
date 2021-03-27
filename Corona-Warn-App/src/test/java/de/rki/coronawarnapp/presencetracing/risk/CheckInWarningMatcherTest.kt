package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.debug.measureTime
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber

class CheckInWarningMatcherTest : BaseTest() {

    @MockK lateinit var checkInsRepository: CheckInRepository
    @MockK lateinit var traceTimeIntervalWarningRepository: TraceTimeIntervalWarningRepository
    @MockK lateinit var presenceTracingRiskRepository: PresenceTracingRiskRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { presenceTracingRiskRepository.replaceAllMatches(any()) } just Runs
        coEvery { presenceTracingRiskRepository.deleteAllMatches() } just Runs
    }

    @Test
    fun `replaces matches`() {
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
            endDateStr = "2021-03-04T10:12+01:00"
        )

        val warning1 = createWarning(
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationGuid = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf(checkIn1, checkIn2))

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val id: Long
                get() = 1L
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) { presenceTracingRiskRepository.replaceAllMatches(any()) }
            coVerify(exactly = 0) { presenceTracingRiskRepository.deleteAllMatches() }
        }
    }

    @Test
    fun `replace with empty list if no matches found`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        val warning1 = createWarning(
            traceLocationGuid = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationGuid = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf(checkIn1, checkIn2))

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val id: Long
                get() = 1L
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) { presenceTracingRiskRepository.replaceAllMatches(emptyList()) }
            coVerify(exactly = 0) { presenceTracingRiskRepository.deleteAllMatches() }
        }
    }

    @Test
    fun `replace with empty list if package is empty`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationGuid = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf(checkIn1, checkIn2))

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf()
            }

            override val id: Long
                get() = 1L
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) { presenceTracingRiskRepository.replaceAllMatches(emptyList()) }
            coVerify(exactly = 0) { presenceTracingRiskRepository.deleteAllMatches() }
        }
    }

    @Test
    fun `deletes all matches if no check-ins`() {

        val warning1 = createWarning(
            traceLocationGuid = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationGuid = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf())

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val id: Long
                get() = 1L
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 0) { presenceTracingRiskRepository.replaceAllMatches(any()) }
            coVerify(exactly = 1) { presenceTracingRiskRepository.deleteAllMatches() }
        }
    }

    @Test
    fun `test mass data`() {
        val checkIns = (1L..100L).map {
            createCheckIn(
                id = it,
                traceLocationGuid = it.toString(),
                startDateStr = "2021-03-04T09:50+01:00",
                endDateStr = "2021-03-04T10:05:15+01:00"
            )
        }
        val warnings = (1L..1000L).map {
            createWarning(
                traceLocationGuid = it.toString(),
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        }

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarning(): List<TraceWarning.TraceTimeIntervalWarning> {
                return warnings
            }

            override val id: Long
                get() = 1L
        }

        every { checkInsRepository.allCheckIns } returns flowOf(checkIns)
        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            measureTime(
                { Timber.d("Time to compare 200 checkIns with 1000 warnings: $it millis") },
                { createInstance().execute() }
            )
        }
    }

    private fun createInstance() = CheckInWarningMatcher(
        checkInsRepository,
        traceTimeIntervalWarningRepository,
        presenceTracingRiskRepository
    )
}
