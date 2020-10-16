package de.rki.coronawarnapp.util.errors

import io.kotest.matchers.shouldBe
import org.junit.Test
import testhelpers.BaseTest
import java.io.IOException

class ExceptionExtensionsTest : BaseTest() {

    @Test
    fun `exception without child cause`() {
        val testException: Throwable = IndexOutOfBoundsException()
        testException.causes().toList().size shouldBe 1
        testException.causes().toList() shouldBe listOf(IndexOutOfBoundsException())
    }

    @Test
    fun `exception with multiple nested causes`() {
        val inner: Throwable =
            IllegalArgumentException(IOException(NullPointerException()))
        val outer: Throwable = IllegalStateException(inner)

        outer.causes().toList().size shouldBe 4
        outer.causes().toList() shouldBe listOf(
            IllegalStateException(IllegalArgumentException(IOException(NullPointerException()))),
            IllegalArgumentException(IOException(NullPointerException())),
            IOException(NullPointerException()),
            NullPointerException()
        )
    }

    @Test
    fun `find specific exception in causes`() {
        val inner: Throwable =
            IllegalArgumentException(IOException(NullPointerException()))
        val outer: Throwable = IllegalStateException(inner)

        outer.causes().toList().size shouldBe 4
        outer.causes().toList() shouldBe listOf(
            IllegalStateException(IllegalArgumentException(IOException(NullPointerException()))),
            IllegalArgumentException(IOException(NullPointerException())),
            IOException(NullPointerException()),
            NullPointerException()
        )
    }
}
