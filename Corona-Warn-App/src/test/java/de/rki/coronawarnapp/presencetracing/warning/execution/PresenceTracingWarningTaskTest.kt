package de.rki.coronawarnapp.presencetracing.warning.execution

import de.rki.coronawarnapp.presencetracing.risk.CheckInWarningMatcher
import de.rki.coronawarnapp.presencetracing.warning.download.TraceWarningPackageSyncTool
import de.rki.coronawarnapp.util.TimeStamper
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PresenceTracingWarningTaskTest : BaseTest() {

    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var syncTool: TraceWarningPackageSyncTool
    @MockK lateinit var checkInWarningMatcher: CheckInWarningMatcher

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { timeStamper.nowUTC } returns Instant.ofEpochMilli(9000)
        coEvery { syncTool.syncPackages() } returns mockk()
        coEvery { checkInWarningMatcher.execute() } returns listOf(mockk())
    }

    private fun createInstance() = PresenceTracingWarningTask(
        timeStamper = timeStamper,
        syncTool = syncTool,
        checkInWarningMatcher = checkInWarningMatcher
    )

    @Test
    fun `no errors`() = runBlockingTest {
        val result = createInstance().run(mockk())

        coVerifyOrder {
            syncTool.syncPackages()
            checkInWarningMatcher.execute()
        }
        result shouldNotBe null
    }
}
