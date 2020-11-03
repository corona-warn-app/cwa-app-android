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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BackgroundModeStatusTest : BaseTest() {

    @MockK lateinit var context: Context
    private val scope: CoroutineScope = TestCoroutineScope()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkObject(ConnectivityHelper)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): BackgroundModeStatus = BackgroundModeStatus(
        context = context,
        appScope = scope
    )

    @Test
    fun `init is sideeffect free and lazy`() {
        createInstance()
        verify { context wasNot Called }
    }

    @Test
    fun isAutoModeEnabled() = runBlockingTest {
        every { ConnectivityHelper.autoModeEnabled(any()) } returnsMany listOf(
            true, false, true, false
        )
        createInstance().apply {
            isAutoModeEnabled.first() shouldBe true
            isAutoModeEnabled.first() shouldBe false
            isAutoModeEnabled.first() shouldBe true
        }
    }

    @Test
    fun isBackgroundRestricted() = runBlockingTest {
        every { ConnectivityHelper.isBackgroundRestricted(any()) } returnsMany listOf(
            false, true, false
        )
        createInstance().apply {
            isBackgroundRestricted.first() shouldBe false
            isBackgroundRestricted.first() shouldBe true
            isBackgroundRestricted.first() shouldBe false
        }
    }
}
