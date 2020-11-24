package de.rki.coronawarnapp.util

import android.content.Context
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2
import testhelpers.coroutines.test

class BackgroundModeStatusTest : BaseTest() {

    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(ConnectivityHelper)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(scope: CoroutineScope): BackgroundModeStatus = BackgroundModeStatus(
        context = context,
        appScope = scope
    )

    @Test
    fun `init is sideeffect free and lazy`() = runBlockingTest2(ignoreActive = true) {
        createInstance(scope = this)
        verify { context wasNot Called }
    }

    @Test
    fun isAutoModeEnabled() = runBlockingTest2(ignoreActive = true) {
        every { ConnectivityHelper.autoModeEnabled(any()) } returnsMany listOf(true, false, true, false)
        createInstance(scope = this).apply {
            isAutoModeEnabled.first() shouldBe true
            isAutoModeEnabled.first() shouldBe false
            isAutoModeEnabled.first() shouldBe true
        }
    }

    @Test
    fun `isAutoModeEnabled is shared but not cached`() = runBlockingTest2(ignoreActive = true) {
        every { ConnectivityHelper.autoModeEnabled(any()) } returnsMany listOf(true, false, true, false)

        val instance = createInstance(scope = this)

        val collector1 = instance.isAutoModeEnabled.test(tag = "1", startOnScope = this)
        val collector2 = instance.isAutoModeEnabled.test(tag = "2", startOnScope = this)

        delay(500)

        collector1.latestValue shouldBe true
        collector2.latestValue shouldBe true

        collector1.cancel()
        collector2.cancel()

        advanceUntilIdle()

        verify(exactly = 1) { ConnectivityHelper.autoModeEnabled(any()) }

        instance.isAutoModeEnabled.first() shouldBe false
    }

    @Test
    fun isBackgroundRestricted() = runBlockingTest2(ignoreActive = true) {
        every { ConnectivityHelper.isBackgroundRestricted(any()) } returnsMany listOf(false, true, false)
        createInstance(scope = this).apply {
            isBackgroundRestricted.first() shouldBe false
            isBackgroundRestricted.first() shouldBe true
            isBackgroundRestricted.first() shouldBe false
        }
    }

    @Test
    fun `isBackgroundRestricted is shared but not cached`() = runBlockingTest2(ignoreActive = true) {
        every { ConnectivityHelper.isBackgroundRestricted(any()) } returnsMany listOf(true, false, true, false)

        val instance = createInstance(scope = this)

        val collector1 = instance.isBackgroundRestricted.test(tag = "1", startOnScope = this)
        val collector2 = instance.isBackgroundRestricted.test(tag = "2", startOnScope = this)

        delay(500)

        collector1.latestValue shouldBe true
        collector2.latestValue shouldBe true

        collector1.cancel()
        collector2.cancel()

        advanceUntilIdle()

        verify(exactly = 1) { ConnectivityHelper.isBackgroundRestricted(any()) }

        instance.isBackgroundRestricted.first() shouldBe false
    }
}
