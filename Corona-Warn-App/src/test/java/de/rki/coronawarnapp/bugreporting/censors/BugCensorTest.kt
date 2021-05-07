package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNewLogLineIfDifferent
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
    fun `description censoring validity`() {
        BugCensor.withValidDescription(null) {} shouldBe false
        BugCensor.withValidDescription("   ") {} shouldBe false
        BugCensor.withValidDescription("        ") {} shouldBe false
        BugCensor.withValidDescription("a") {} shouldBe false
        BugCensor.withValidDescription("ab") {} shouldBe false
        BugCensor.withValidDescription("abc") {} shouldBe false
        BugCensor.withValidDescription("abcd") {} shouldBe false
        BugCensor.withValidDescription("abcde") {} shouldBe true
    }

    @Test
    fun `address censoring validity`() {
        BugCensor.withValidAddress(null) {} shouldBe false
        BugCensor.withValidAddress("   ") {} shouldBe false
        BugCensor.withValidAddress("        ") {} shouldBe false
        BugCensor.withValidAddress("a") {} shouldBe false
        BugCensor.withValidAddress("ab") {} shouldBe false
        BugCensor.withValidAddress("abc") {} shouldBe false
        BugCensor.withValidAddress("abcd") {} shouldBe true
    }

    @Test
    fun `city censoring validity`() {
        BugCensor.withValidCity(null) {} shouldBe false
        BugCensor.withValidCity("   ") {} shouldBe false
        BugCensor.withValidCity("        ") {} shouldBe false
        BugCensor.withValidCity("a") {} shouldBe false
        BugCensor.withValidCity("ab") {} shouldBe false
        BugCensor.withValidCity("abc") {} shouldBe true
    }

    @Test
    fun `zip-code censoring validity`() {
        BugCensor.withValidZipCode(null) {} shouldBe false
        BugCensor.withValidZipCode("   ") {} shouldBe false
        BugCensor.withValidZipCode("        ") {} shouldBe false
        BugCensor.withValidZipCode("1") {} shouldBe false
        BugCensor.withValidZipCode("12") {} shouldBe false
        BugCensor.withValidZipCode("123") {} shouldBe false
        BugCensor.withValidZipCode("1234") {} shouldBe false
        BugCensor.withValidZipCode("12345") {} shouldBe true
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
        logLine.toNewLogLineIfDifferent("Message") shouldBe null
        logLine.toNewLogLineIfDifferent("Message ") shouldNotBe logLine
    }
}
