package de.rki.coronawarnapp.datadonation.analytics.ui.input

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class AnalyticsUserInputViewModelTest : BaseTest() {
    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `test agegroup emission`() {
        TODO()
    }

    @Test
    fun `test agegroup selection`() {
        TODO()
    }

    @Test
    fun `test federal state emission`() {
        TODO()
    }

    @Test
    fun `test federal state selection`() {
        TODO()
    }

    @Test
    fun `test district emission`() {
        TODO()
    }

    @Test
    fun `test district selection`() {
        TODO()
    }
}
