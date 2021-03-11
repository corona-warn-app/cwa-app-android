package de.rki.coronawarnapp.bugreporting.censors

import io.kotest.matchers.shouldBe
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
        BugCensor.withValidName("Jo") {} shouldBe true
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
}
