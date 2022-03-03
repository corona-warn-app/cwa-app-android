package de.rki.coronawarnapp.presencetracing.risk.execution

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.presencetracing.checkins.CheckInRepository
import de.rki.coronawarnapp.presencetracing.checkins.checkout.auto.AutoCheckOut
import de.rki.coronawarnapp.presencetracing.risk.calculation.CheckInWarningMatcher
import de.rki.coronawarnapp.presencetracing.risk.calculation.PresenceTracingRiskMapper
import de.rki.coronawarnapp.presencetracing.risk.calculation.createCheckIn
import de.rki.coronawarnapp.presencetracing.risk.calculation.createWarning
import de.rki.coronawarnapp.presencetracing.risk.storage.PresenceTracingRiskRepository
import de.rki.coronawarnapp.presencetracing.warning.WarningPackageId
import de.rki.coronawarnapp.presencetracing.warning.download.TraceWarningPackageSyncTool
import de.rki.coronawarnapp.presencetracing.warning.download.server.TraceWarningApi
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningPackage
import de.rki.coronawarnapp.presencetracing.warning.storage.TraceWarningRepository
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Duration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.IOException

class PresenceTracingWarningTaskTest : BaseTest() {

    @MockK lateinit var syncTool: TraceWarningPackageSyncTool
    @MockK lateinit var checkInWarningMatcher: CheckInWarningMatcher
    @MockK lateinit var presenceTracingRiskRepository: PresenceTracingRiskRepository
    @MockK lateinit var traceWarningRepository: TraceWarningRepository
    @MockK lateinit var checkInsRepository: CheckInRepository
    @MockK lateinit var presenceTracingRiskMapper: PresenceTracingRiskMapper
    @MockK lateinit var autoCheckOut: AutoCheckOut
    @MockK lateinit var appConfigProvider: AppConfigProvider

    private val mode = TraceWarningApi.Mode.UNENCRYPTED

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appConfigProvider.getAppConfig() } returns mockk<ConfigData>().apply {
            every { isUnencryptedCheckInsEnabled } returns true
        }

        coEvery { syncTool.syncPackages(any()) } returns TraceWarningPackageSyncTool.SyncResult(successful = true)
        coEvery { checkInWarningMatcher.process(any(), any()) } answers {
            CheckInWarningMatcher.Result(
                successful = true,
                processedPackages = listOf(
                    CheckInWarningMatcher.MatchesPerPackage(
                        warningPackage = WARNING_PKG,
                        overlaps = listOf(mockk())
                    )
                )
            )
        }

        traceWarningRepository.apply {
            coEvery { unprocessedWarningPackages } returns flowOf(listOf(WARNING_PKG))
            coEvery { markPackagesProcessed(any()) } just Runs
        }

        coEvery { checkInsRepository.checkInsWithinRetention } returns flowOf(listOf(CHECKIN_1, CHECKIN_2))

        presenceTracingRiskRepository.apply {
            coEvery { deleteAllMatches() } just Runs
            coEvery { deleteStaleData() } just Runs
            coEvery { reportCalculation(any(), any()) } just Runs
        }

        coEvery { presenceTracingRiskMapper.clearConfig() } just Runs

        coEvery { autoCheckOut.processOverDueCheckouts() } returns emptyList()
        coEvery { autoCheckOut.refreshAlarm() } returns true
    }

    private fun createInstance() = PresenceTracingWarningTask(
        syncTool = syncTool,
        checkInWarningMatcher = checkInWarningMatcher,
        presenceTracingRiskRepository = presenceTracingRiskRepository,
        traceWarningRepository = traceWarningRepository,
        checkInsRepository = checkInsRepository,
        presenceTracingRiskMapper = presenceTracingRiskMapper,
        autoCheckOut = autoCheckOut,
        appConfigProvider = appConfigProvider
    )

    @Test
    fun `happy path, match result is reported successfully`() = runBlockingTest {
        createInstance().run(mockk()) shouldNotBe null

        coVerifySequence {
            autoCheckOut.processOverDueCheckouts()
            autoCheckOut.refreshAlarm()

            syncTool.syncPackages(mode)
            presenceTracingRiskRepository.deleteStaleData()
            checkInsRepository.checkInsWithinRetention
            traceWarningRepository.unprocessedWarningPackages

            checkInWarningMatcher.process(any(), any())

            presenceTracingRiskRepository.reportCalculation(
                successful = true,
                overlaps = any()
            )
            traceWarningRepository.markPackagesProcessed(listOf(WARNING_PKG.packageId))
        }
    }

    @Test
    fun `happy path with config change`() = runBlockingTest {
        createInstance().run(mockk()) shouldNotBe null

        coVerifySequence {
            presenceTracingRiskMapper.clearConfig()
            syncTool.syncPackages(mode)
            presenceTracingRiskRepository.deleteStaleData()
            checkInsRepository.checkInsWithinRetention
            traceWarningRepository.unprocessedWarningPackages

            checkInWarningMatcher.process(any(), any())

            presenceTracingRiskRepository.reportCalculation(
                successful = true,
                overlaps = any()
            )
            traceWarningRepository.markPackagesProcessed(listOf(WARNING_PKG.packageId))
        }
    }

    @Test
    fun `overall task errors lead to a reported failed calculation`() = runBlockingTest {
        coEvery { syncTool.syncPackages(mode) } throws IOException("Unexpected")

        shouldThrow<IOException> {
            createInstance().run(mockk())
        }

        coVerify {
            presenceTracingRiskRepository.reportCalculation(
                successful = false,
                overlaps = emptyList()
            )
        }
    }

    @Test
    fun `there are no check-ins to match against`() = runBlockingTest {
        coEvery { checkInsRepository.checkInsWithinRetention } returns flowOf(emptyList())

        createInstance().run(mockk()) shouldNotBe null

        coVerifySequence {
            syncTool.syncPackages(mode)
            presenceTracingRiskRepository.deleteStaleData()
            checkInsRepository.checkInsWithinRetention

            presenceTracingRiskRepository.deleteAllMatches()
            presenceTracingRiskRepository.reportCalculation(successful = true)
        }
    }

    @Test
    fun `there are no warning packages to process`() = runBlockingTest {
        coEvery { traceWarningRepository.unprocessedWarningPackages } returns flowOf(emptyList())

        createInstance().run(mockk()) shouldNotBe null

        coVerifySequence {
            syncTool.syncPackages(mode)
            presenceTracingRiskRepository.deleteStaleData()
            checkInsRepository.checkInsWithinRetention
            traceWarningRepository.unprocessedWarningPackages

            presenceTracingRiskRepository.reportCalculation(successful = true)
        }
    }

    @Test
    fun `report failure if downloads fail`() = runBlockingTest {
        coEvery { syncTool.syncPackages(mode) } returns TraceWarningPackageSyncTool.SyncResult(successful = false)

        createInstance().run(mockk()) shouldNotBe null

        coVerifySequence {
            syncTool.syncPackages(mode)

            presenceTracingRiskRepository.reportCalculation(
                successful = false,
                overlaps = emptyList()
            )
        }

        coVerify(exactly = 0) {
            traceWarningRepository.markPackagesProcessed(any())
        }
    }

    @Test
    fun `report failure if matching throws exception`() = runBlockingTest {
        coEvery { checkInWarningMatcher.process(any(), any()) } throws IllegalArgumentException()
        shouldThrow<IllegalArgumentException> {
            createInstance().run(mockk()) shouldNotBe null
        }

        coVerifySequence {
            syncTool.syncPackages(mode)
            presenceTracingRiskRepository.deleteStaleData()
            checkInsRepository.checkInsWithinRetention
            traceWarningRepository.unprocessedWarningPackages

            checkInWarningMatcher.process(any(), any())

            presenceTracingRiskRepository.reportCalculation(
                successful = false,
                overlaps = any()
            )
        }

        coVerify(exactly = 0) {
            traceWarningRepository.markPackagesProcessed(any())
        }
    }

    @Test
    fun `task timeout is constrained to less than 9min`() {
        // Worker execution time
        val maxDuration = Duration.standardMinutes(9).plus(1)
        PresenceTracingWarningTask.Config().executionTimeout shouldBeLessThan maxDuration
    }

    companion object {
        val CHECKIN_1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )
        val CHECKIN_2 = createCheckIn(
            id = 3L,
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startDateStr = "2021-03-04T09:15+01:00",
            endDateStr = "2021-03-04T10:12+01:00"
        )

        val WARNING_1 = createWarning(
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val WARNING_2 = createWarning(
            traceLocationId = "69eb427e1a48133970486244487e31b3f1c5bde47415db9b52cc5a2ece1e0060",
            startIntervalDateStr = "2021-03-04T10:00+01:00",
            period = 6,
            transmissionRiskLevel = 8
        )

        val WARNING_PKG = object : TraceWarningPackage {
            override suspend fun extractUnencryptedWarnings(): List<TraceWarning.TraceTimeIntervalWarning> {
                return listOf(WARNING_1, WARNING_2)
            }

            override suspend fun extractEncryptedWarnings() = emptyList<CheckInOuterClass.CheckInProtectedReport>()

            override val packageId: WarningPackageId
                get() = "id"
        }
    }
}
