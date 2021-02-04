package de.rki.coronawarnapp.util

import android.content.res.Configuration
import android.content.res.Resources
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class ContextExtensionsTests {
    @MockK
    private lateinit var resources: Resources

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `500dp smallestScreenWidth is phone`() {
        every { resources.configuration } returns Configuration().apply {
            smallestScreenWidthDp = 500
        }

        resources.isPhone() shouldBe true
    }

    @Test
    fun `600dp smallestScreenWidth is not phone`() {
        every { resources.configuration } returns Configuration().apply {
            smallestScreenWidthDp = 600
        }

        resources.isPhone() shouldBe false
    }
}
