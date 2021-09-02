package de.rki.coronawarnapp.covidcertificate.pdf.core

import android.graphics.Paint
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UtilsTest {

    @MockK lateinit var paint: Paint

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun getMultilineTextTest() {

        val slot = slot<String>()
        every { paint.measureText(capture(slot)) } answers {
            slot.captured.length.toFloat()
        }

        val text = "ABCDEFG 12345 XYZ OMG 0123456789101112"
        val result = listOf("ABCDEFG", "12345 XYZ", "OMG", "012345678", "9101112")

        getMultilineText(text, paint, 9) shouldBe result
    }
}
