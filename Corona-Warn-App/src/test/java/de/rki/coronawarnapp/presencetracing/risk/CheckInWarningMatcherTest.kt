package de.rki.coronawarnapp.presencetracing.risk

import de.rki.coronawarnapp.eventregistration.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackage
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
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
import testhelpers.TestDispatcherProvider
import timber.log.Timber

class CheckInWarningMatcherTest : BaseTest() {

    @MockK lateinit var checkInsRepository: CheckInRepository
    @MockK lateinit var traceWarningRepository: TraceWarningRepository
    @MockK lateinit var presenceTracingRiskRepository: PresenceTracingRiskRepository

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { presenceTracingRiskRepository.reportSuccessfulCalculation(any()) } just Runs
        coEvery { presenceTracingRiskRepository.deleteAllMatches() } just Runs

        coEvery { traceWarningRepository.markPackageProcessed(any()) } just Runs
        coEvery { presenceTracingRiskRepository.deleteStaleData() } just Runs
        // TODO tests
        coEvery { presenceTracingRiskRepository.deleteMatchesOfPackage(any()) } just Runs
    }

    private fun createInstance() = CheckInWarningMatcher(
        checkInsRepository,
        traceWarningRepository,
        presenceTracingRiskRepository,
        TestDispatcherProvider()
    )

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

        val warningPackage = object : TraceWarningPackage {
            override suspend fun extractWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val packageId: WarningPackageId
                get() = "id"
        }

        every { traceWarningRepository.unprocessedWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.reportSuccessfulCalculation(any())
                traceWarningRepository.markPackageProcessed(warningPackage.packageId)
            }
            coVerify(exactly = 0) { presenceTracingRiskRepository.deleteAllMatches() }
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

        val warningPackage = object : TraceWarningPackage {
            override suspend fun extractWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val packageId: WarningPackageId
                get() = "id"
        }

        every { traceWarningRepository.unprocessedWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.reportSuccessfulCalculation(emptyList())
                traceWarningRepository.markPackageProcessed(warningPackage.packageId)
            }
            coVerify(exactly = 0) { presenceTracingRiskRepository.deleteAllMatches() }
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

        val warningPackage = object : TraceWarningPackage {
            override suspend fun extractWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf()
            }

            override val packageId: WarningPackageId
                get() = "id"
        }

        every { traceWarningRepository.unprocessedWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) {
                presenceTracingRiskRepository.reportSuccessfulCalculation(emptyList())
                traceWarningRepository.markPackageProcessed(warningPackage.packageId)
            }
            coVerify(exactly = 0) { presenceTracingRiskRepository.deleteAllMatches() }
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

        val warningPackage = object : TraceWarningPackage {
            override suspend fun extractWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(warning1, warning2)
            }

            override val packageId: WarningPackageId
                get() = "id"
        }

        every { traceWarningRepository.unprocessedWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            createInstance().execute()
            coVerify(exactly = 1) { presenceTracingRiskRepository.reportSuccessfulCalculation(emptyList()) }
            coVerify(exactly = 1) { presenceTracingRiskRepository.deleteAllMatches() }
        }
    }

    @Test
    fun `test mass data`() {
        val checkIns = (1L..100L).map {
            createCheckIn(
                id = it,
                traceLocationId = it.toString(),
                startDateStr = "2021-03-04T09:50+01:00",
                endDateStr = "2021-03-04T10:05:15+01:00"
            )
        }
        val warnings = (1L..1000L).map {
            createWarning(
                traceLocationId = it.toString(),
                startIntervalDateStr = "2021-03-04T10:00+01:00",
                period = 6,
                transmissionRiskLevel = 8
            )
        }

        val warningPackage = object : TraceWarningPackage {
            override suspend fun extractWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return warnings
            }

            override val packageId: WarningPackageId
                get() = "id"
        }

        every { checkInsRepository.allCheckIns } returns flowOf(checkIns)
        every { traceWarningRepository.unprocessedWarningPackages } returns flowOf(listOf(warningPackage))

        runBlockingTest {
            measureTime(
                { Timber.d("Time to compare 200 checkIns with 1000 warnings: $it millis") },
                { createInstance().execute() }
            )
        }
    }

    @Test
    fun `warning packages are marked as processed`() {
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

        val warningPackage1 = object : TraceWarningPackage {
            override suspend fun extractWarnings() = listOf(
                createWarning(
                    traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
                    startIntervalDateStr = "2021-03-04T10:00+01:00",
                    period = 6,
                    transmissionRiskLevel = 8
                )
            )

            override val packageId: WarningPackageId = "id1"
        }
        val warningPackage2 = object : TraceWarningPackage {
            override suspend fun extractWarnings() = listOf(
                createWarning(
                    traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
                    startIntervalDateStr = "2021-03-04T10:00+01:00",
                    period = 6,
                    transmissionRiskLevel = 8
                )
            )

            override val packageId: WarningPackageId = "id2"
        }

        every { traceWarningRepository.unprocessedWarningPackages } returns flowOf(
            listOf(warningPackage1, warningPackage2)
        )

        runBlockingTest {
            createInstance().execute()
        }

        coVerify(exactly = 1) {
            presenceTracingRiskRepository.reportSuccessfulCalculation(any())
            traceWarningRepository.markPackageProcessed(warningPackage1.packageId)
            traceWarningRepository.markPackageProcessed(warningPackage2.packageId)
        }
        coVerify(exactly = 0) { presenceTracingRiskRepository.deleteAllMatches() }
    }

//    @Test
//    fun `partial processing is possible on exceptions`() {
//        val checkIn1 = createCheckIn(
//            id = 2L,
//            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
//            startDateStr = "2021-03-04T10:15+01:00",
//            endDateStr = "2021-03-04T10:17+01:00"
//        )
//        val checkIn2 = createCheckIn(
//            id = 3L,
//            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
//            startDateStr = "2021-03-04T09:15+01:00",
//            endDateStr = "2021-03-04T10:12+01:00"
//        )
//        every { checkInsRepository.allCheckIns } returns flowOf(listOf(checkIn1, checkIn2))
//
//        val warningPackage1 = object : TraceWarningPackage {
//            override suspend fun extractWarnings() = throw Exception()
//
//            override val packageId: WarningPackageId = "id1"
//        }
//        val warningPackage2 = object : TraceWarningPackage {
//            override suspend fun extractWarnings() = listOf(
//                createWarning(
//                    traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
//                    startIntervalDateStr = "2021-03-04T10:00+01:00",
//                    period = 6,
//                    transmissionRiskLevel = 8
//                )
//            )
//
//            override val packageId: WarningPackageId = "id2"
//        }
//
//        every { traceWarningRepository.unprocessedWarningPackages } returns flowOf(
//            listOf(warningPackage1, warningPackage2)
//        )
//
//        runBlockingTest {
//            createInstance().execute()
//        }
//
//        coVerify(exactly = 1) {
//            traceWarningRepository.markPackageProcessed(warningPackage2.packageId)
//        }
//        coVerify(exactly = 0) {
//            presenceTracingRiskRepository.reportSuccessfulCalculation(any())
//            presenceTracingRiskRepository.deleteAllMatches()
//        }
//    }
}
