package de.rki.coronawarnapp.util

import android.app.ActivityManager
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.util.device.BackgroundModeStatus
import de.rki.coronawarnapp.util.device.ForegroundState
import de.rki.coronawarnapp.util.device.PowerManagement
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.coroutines.test

class BackgroundModeStatusTest : BaseTest() {

    @MockK lateinit var activityManager: ActivityManager
    @MockK lateinit var foregroundState: ForegroundState
    @MockK lateinit var powerManagement: PowerManagement
    @MockK lateinit var apiLevel: ApiLevel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildConfigWrap)

        every { apiLevel.hasAPILevel(any()) } returns true

        every { foregroundState.isInForeground } returns flowOf(true)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(scope: CoroutineScope): BackgroundModeStatus = BackgroundModeStatus(
        activityManager = activityManager,
        appScope = scope,
        foregroundState = foregroundState,
        powerManagement = powerManagement,
        apiLevel = apiLevel
    )

    @Test
    fun `init is sideeffect free and lazy`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this)
        verify { activityManager wasNot Called }
    }

    @Test
    fun isAutoModeEnabled() = runBlockingTest2(ignoreActive = true) {
        every { activityManager.isBackgroundRestricted } returnsMany listOf(true, false, true, false)
        createInstance(scope = this).apply {
            isAutoModeEnabled.first() shouldBe true
            isAutoModeEnabled.first() shouldBe false
            isAutoModeEnabled.first() shouldBe true
        }
    }

    @Test
    fun `autoMode enabled means battery optimizations are ignored or we are not restricted`() {
        runBlockingTest2(ignoreActive = true) {
            every { activityManager.isBackgroundRestricted } returns true
            every { powerManagement.isIgnoringBatteryOptimizations } returns false
            createInstance(scope = this).isAutoModeEnabled.first() shouldBe false
        }
        runBlockingTest2(ignoreActive = true) {
            every { activityManager.isBackgroundRestricted } returns false
            every { powerManagement.isIgnoringBatteryOptimizations } returns false
            createInstance(scope = this).isAutoModeEnabled.first() shouldBe true
        }
        runBlockingTest2(ignoreActive = true) {
            every { activityManager.isBackgroundRestricted } returns true
            every { powerManagement.isIgnoringBatteryOptimizations } returns true
            createInstance(scope = this).isAutoModeEnabled.first() shouldBe true
        }
        runBlockingTest2(ignoreActive = true) {
            every { activityManager.isBackgroundRestricted } returns false
            every { powerManagement.isIgnoringBatteryOptimizations } returns true
            createInstance(scope = this).isAutoModeEnabled.first() shouldBe true
        }
    }

    @Test
    fun `isAutoModeEnabled is shared but not cached`() = runBlockingTest2(ignoreActive = true) {
        every { powerManagement.isIgnoringBatteryOptimizations } returnsMany listOf(true, false, true, false)
        every { activityManager.isBackgroundRestricted } returns true

        val instance = createInstance(scope = this)

        val collector1 = instance.isAutoModeEnabled.test(tag = "1", startOnScope = this)
        val collector2 = instance.isAutoModeEnabled.test(tag = "2", startOnScope = this)

        delay(500)

        collector1.latestValue shouldBe true
        collector2.latestValue shouldBe true

        collector1.cancel()
        collector2.cancel()

        advanceUntilIdle()

        verify(exactly = 1) { activityManager.isBackgroundRestricted }

        instance.isAutoModeEnabled.first() shouldBe false
    }

    @Test
    fun isBackgroundRestricted() = runBlockingTest2(ignoreActive = true) {
        every { activityManager.isBackgroundRestricted } returnsMany listOf(false, true, false)
        createInstance(scope = this).apply {
            isBackgroundRestricted.first() shouldBe false
            isBackgroundRestricted.first() shouldBe true
            isBackgroundRestricted.first() shouldBe false
        }
    }

    @Test
    fun `isBackgroundRestricted is shared but not cached`() = runBlockingTest2(ignoreActive = true) {
        every { activityManager.isBackgroundRestricted } returnsMany listOf(true, false, true, false)

        val instance = createInstance(scope = this)

        val collector1 = instance.isBackgroundRestricted.test(tag = "1", startOnScope = this)
        val collector2 = instance.isBackgroundRestricted.test(tag = "2", startOnScope = this)

        delay(500)

        collector1.latestValue shouldBe true
        collector2.latestValue shouldBe true

        collector1.cancel()
        collector2.cancel()

        advanceUntilIdle()

        verify(exactly = 1) { activityManager.isBackgroundRestricted }

        instance.isBackgroundRestricted.first() shouldBe false
    }

    @Test
    fun `isBackgroundRestricted defaults to false on API27 and lower`() = runBlockingTest2(ignoreActive = true) {
        every { activityManager.isBackgroundRestricted } returns true

        createInstance(scope = this).isBackgroundRestricted.first() shouldBe true

        every { apiLevel.hasAPILevel(any()) } returns false

        createInstance(scope = this).isBackgroundRestricted.first() shouldBe false
    }
}
