package de.rki.coronawarnapp.util

import android.content.Context
import io.mockk.Called
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BackgroundModeStatusTest : BaseTest() {

    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): BackgroundModeStatus = BackgroundModeStatus(
        context = context
    )

    @Test
    fun `init is sideeffect free and lazy`() {
        createInstance()
        verify { context wasNot Called }
    }

    @Test
    fun `TBD`() = runBlockingTest {
        TODO()
    }
}
