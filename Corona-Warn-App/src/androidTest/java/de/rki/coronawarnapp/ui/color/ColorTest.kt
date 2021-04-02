package de.rki.coronawarnapp.ui.color

import android.graphics.Color
import io.kotest.matchers.shouldBe
import org.junit.Test

import testhelpers.BaseTestInstrumentation

class ColorTest : BaseTestInstrumentation() {

    @Test
    fun parseValidColor() {
        "#FFFFFF".parseColor() shouldBe Color.WHITE
    }

    @Test
    fun parseInvalidColor() {
        "000000".parseColor() shouldBe Color.BLACK
    }

    @Test
    fun defaultColor() {
        "00".parseColor(Color.GRAY) shouldBe Color.GRAY
    }
}
