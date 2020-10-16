package de.rki.coronawarnapp.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import kotlin.math.abs
import kotlin.random.Random

class PaddingToolTest : BaseTest() {

    private val validPattern = "^([A-Za-z0-9]+)$".toRegex()

    @Test
    fun `verify padding patterns`() {
        repeat(1000) {
            val randomLength = abs(Random.nextInt(1, 1024))
            PaddingTool.requestPadding(randomLength).apply {
                length shouldBe randomLength
                Timber.v("RandomLength: %d, Padding: %s", randomLength, this)
                validPattern.matches(this) shouldBe true
            }
        }
    }
}
