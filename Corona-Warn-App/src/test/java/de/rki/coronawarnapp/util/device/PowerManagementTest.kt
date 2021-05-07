package de.rki.coronawarnapp.util.device

import android.content.Context
import de.rki.coronawarnapp.util.BuildVersionWrap
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.MockKException
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import testhelpers.BaseTest
import testhelpers.extensions.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
class PowerManagementTest : BaseTest() {

    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildVersionWrap)
        every { BuildVersionWrap.SDK_INT } returns 23
    }

    fun createInstance() = PowerManagement(
        context = context
    )

    @Test
    fun `isIgnoringBatteryOptimizations always returns true below API23`() {
        val instance = createInstance()

        shouldThrow<MockKException> {
            instance.isIgnoringBatteryOptimizations
        }

        every { BuildVersionWrap.SDK_INT } returns 22

        instance.isIgnoringBatteryOptimizations shouldBe true
    }
}
