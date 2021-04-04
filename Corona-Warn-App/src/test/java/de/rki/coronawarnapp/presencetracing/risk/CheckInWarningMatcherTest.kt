package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningPackage
import de.rki.coronawarnapp.eventregistration.checkins.download.TraceTimeIntervalWarningRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
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
import testhelpers.TestDispatcherProvider

class CheckInWarningMatcherTest : BaseTest() {

    @MockK lateinit var checkInsRepository: CheckInRepository
    @MockK lateinit var traceTimeIntervalWarningRepository: TraceTimeIntervalWarningRepository
    @MockK lateinit var presenceTracingRiskRepository: PresenceTracingRiskRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { presenceTracingRiskRepository.reportSuccessfulCalculation(any(), any()) } just Runs
        coEvery { presenceTracingRiskRepository.deleteAllMatches() } just Runs
        coEvery { presenceTracingRiskRepository.deleteStaleData() } just Runs
        coEvery { presenceTracingRiskRepository.reportFailedCalculation() } just Runs
    }

    @Test
    fun `reports new matches`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        val warning1 = createWarning(
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf(checkIn1, checkIn2))

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val warningPackageId: String
                get() = "id"
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.reportSuccessfulCalculation(
                    listOf(warningPackage), any()
                )
            }
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.deleteAllMatches()
            }
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.reportFailedCalculation()
            }
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.deleteStaleData()
            }
        }
    }

    @Test
    fun `report empty list if no matches found`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        val warning1 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf(checkIn1, checkIn2))

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val warningPackageId: String
                get() = "id"
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.reportSuccessfulCalculation(listOf(warningPackage), emptyList())
            }
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.deleteAllMatches()
            }
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.reportFailedCalculation()
            }
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.deleteStaleData()
            }
        }
    }

    @Test
    fun `report empty list if package is empty`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf(checkIn1, checkIn2))

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf()
            }

            override val warningPackageId: String
                get() = "id"
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.reportSuccessfulCalculation(
                    warningPackages = listOf(warningPackage),
                    overlapList = emptyList()
                )
            }
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.deleteAllMatches()
            }
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.reportFailedCalculation()
            }
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.deleteStaleData()
            }
        }
    }

    @Test
    fun `deletes all matches if no check-ins`() {
        val warning1 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val warning2 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf())

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val warningPackageId: String
                get() = "id"
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.reportSuccessfulCalculation(listOf(warningPackage), emptyList())
            }
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.deleteAllMatches()
            }
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.reportFailedCalculation()
            }
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.deleteStaleData()
            }
        }
    }

    @Test
    fun `report failure if matching throws exception`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val checkIn2 = createCheckIn(
            id = 3L,
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        every { checkInsRepository.allCheckIns } returns flowOf(listOf(checkIn1, checkIn2))

        val warningPackage = object : TraceTimeIntervalWarningPackage {
            override suspend fun extractTraceTimeIntervalWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                throw Exception()
            }

            override val warningPackageId: String
                get() = "id"
        }

        every { traceTimeIntervalWarningRepository.allWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.reportSuccessfulCalculation(
                    listOf(warningPackage), any()
                )
            }
            coVerify(exactly = 0) {
                presenceTracingRiskRepository.deleteAllMatches()
            }
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.reportFailedCalculation()
            }
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.deleteStaleData()
            }
        }
    }

    private fun createInstance() = CheckInWarningMatcher(
        checkInsRepository,
        traceTimeIntervalWarningRepository,
        presenceTracingRiskRepository,
        TestDispatcherProvider()
    )
}
