package de.rki.coronawarnapp.risk

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.task.Task
import de.rki.coronawarnapp.util.BackgroundModeStatus
import de.rki.coronawarnapp.util.TimeStamper
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class RiskLevelTaskTest : BaseTest() {

    @MockK lateinit var riskLevels: RiskLevels
    @MockK lateinit var context: Context
    @MockK lateinit var enfClient: ENFClient
    @MockK lateinit var timeStamper: TimeStamper
    @MockK lateinit var backgroundModeStatus: BackgroundModeStatus
    @MockK lateinit var riskLevelData: RiskLevelData
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var appConfigProvider: AppConfigProvider

    private val arguments: Task.Arguments = object : Task.Arguments {}

    private fun createTask() = RiskLevelTask(
        riskLevels = riskLevels,
        context = context,
        enfClient = enfClient,
        timeStamper = timeStamper,
        backgroundModeStatus = backgroundModeStatus,
        riskLevelData = riskLevelData,
        appConfigProvider = appConfigProvider
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { appConfigProvider.getAppConfig() } returns configData
        every { configData.identifier } returns "config-identifier"

        every { context.getSystemService(Context.CONNECTIVITY_SERVICE) } returns mockk<ConnectivityManager>().apply {
            every { activeNetwork } returns mockk<Network>().apply {
                every { getNetworkCapabilities(any()) } returns mockk<NetworkCapabilities>().apply {
                    every { hasCapability(any()) } returns true
                }
            }
        }

        every { enfClient.isTracingEnabled } returns flowOf(true)
        every { timeStamper.nowUTC } returns Instant.EPOCH
        every { riskLevels.updateRepository(any(), any()) } just Runs

        every { riskLevelData.lastUsedConfigIdentifier = any() } just Runs
    }

    @Test
    fun `last used config ID is set after calculation`() = runBlockingTest {
        every { riskLevels.calculationNotPossibleBecauseOfNoKeys() } returns true
        val task = createTask()
        task.run(arguments)

        verify { riskLevelData.lastUsedConfigIdentifier = "config-identifier" }
    }
}
