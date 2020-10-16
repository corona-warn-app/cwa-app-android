package de.rki.coronawarnapp.ui.settings.start

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class SettingsTracingStateTest : BaseTest() {

    @MockK(relaxed = true) lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `state mapping`() {
        TODO()
    }

    @Test
    fun `bluetooth disabled`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `location disabled`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `tracing inactive`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }

    @Test
    fun `tracing active`() {
        // See TracingHeaderStateTest as guideline
        TODO()
    }
}
