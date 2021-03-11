package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.tryNewMessage
import de.rki.coronawarnapp.bugreporting.debuglog.LogLine
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BugCensorTest : BaseTest() {

    @Test
    fun `name censoring validity`() {
        BugCensor.withValidName(null) {} shouldBe false
        BugCensor.withValidName("") {} shouldBe false
        BugCensor.withValidName("  ") {} shouldBe false
        BugCensor.withValidName("       ") {} shouldBe false
        BugCensor.withValidName("J") {} shouldBe false
        BugCensor.withValidName("Jo") {} shouldBe false
        BugCensor.withValidName("Joe") {} shouldBe true
    }

    @Test
    fun `email censoring validity`() {
        BugCensor.withValidEmail(null) {} shouldBe false
        BugCensor.withValidEmail("") {} shouldBe false
        BugCensor.withValidEmail("     ") {} shouldBe false
        BugCensor.withValidEmail("      ") {} shouldBe false
        BugCensor.withValidEmail("@") {} shouldBe false
        BugCensor.withValidEmail("@.") {} shouldBe false
        BugCensor.withValidEmail("@.de") {} shouldBe false
        BugCensor.withValidEmail("a@.de") {} shouldBe false
        BugCensor.withValidEmail("a@b.de") {} shouldBe true
    }

    @Test
    fun `phone censoring validity`() {
        BugCensor.withValidPhoneNumber(null) {} shouldBe false
        BugCensor.withValidPhoneNumber("    ") {} shouldBe false
        BugCensor.withValidPhoneNumber("        ") {} shouldBe false
        BugCensor.withValidPhoneNumber("0") {} shouldBe false
        BugCensor.withValidPhoneNumber("01") {} shouldBe false
        BugCensor.withValidPhoneNumber("012") {} shouldBe false
        BugCensor.withValidPhoneNumber("0123") {} shouldBe true
    }

    @Test
    fun `comment censoring validity`() {
        BugCensor.withValidComment(null) {} shouldBe false
        BugCensor.withValidComment("   ") {} shouldBe false
        BugCensor.withValidComment("        ") {} shouldBe false
        BugCensor.withValidComment("a") {} shouldBe false
        BugCensor.withValidComment("ab") {} shouldBe false
        BugCensor.withValidComment("abc") {} shouldBe true
    }

    @Test
    fun `loglines are only copied if the message is different`() {
        val logLine = LogLine(
            timestamp = 1,
            priority = 3,
            message = "Message",
            tag = "Tag",
            throwable = null
        )
        logLine.tryNewMessage("Message") shouldBe null
        logLine.tryNewMessage("Message ") shouldNotBe logLine
    }
}
