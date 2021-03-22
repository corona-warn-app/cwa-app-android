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

    @Test
    fun `keyPadding - fake requests with 0 keys`() {
        // requestPadding = 14 keys x 28 bytes per key = 392 bytes`
        PaddingTool.keyPadding(keyListSize = 0).toByteArray().size shouldBe 392
    }

    @Test
    fun `genuine request with 5 keys`() {
        // requestPadding = 9 keys x 28 bytes per key = 252 bytes`
        PaddingTool.keyPadding(keyListSize = 5).toByteArray().size shouldBe 252
    }

    @Test
    fun `genuine request with 16 keys`() {
        //requestPadding = 0 keys x 28 bytes per key = 0 bytes`
        PaddingTool.keyPadding(keyListSize = 16).toByteArray().size shouldBe 0
    }
}
