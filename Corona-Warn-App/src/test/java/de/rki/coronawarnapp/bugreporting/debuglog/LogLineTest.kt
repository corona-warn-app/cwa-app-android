package de.rki.coronawarnapp.bugreporting.debuglog

import android.util.Log
import de.rki.coronawarnapp.util.trimToLength
import io.kotest.matchers.shouldBe
import okio.IOException
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class LogLineTest : BaseTest() {

    @Test
    fun `log formatting`() {
        LogLine(
            timestamp = 123L,
            priority = Log.ERROR,
            tag = "IamATag",
            message = "Low storage check failed.",
            throwable = null
        ).format() shouldBe """
            1970-01-01T00:00:00.123Z E/IamATag: Low storage check failed.
            
        """.trimIndent()
    }

    @Test
    fun `log formatting with error`() {
        LogLine(
            timestamp = 123L,
            priority = Log.ERROR,
            tag = "IamATag",
            message = "Low storage check failed.",
            throwable = IOException()
        ).format().trimToLength(183) shouldBe """
            1970-01-01T00:00:00.123Z E/IamATag: Low storage check failed.

            java.io.IOException
            	at de.rki.coronawarnapp.bugreporting.debuglog.LogLineTest.log formatting with error(LogLineTest.kt:
        """.trimIndent()
    }
}
