package de.rki.coronawarnapp.bugreporting.censors

import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.censor
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.plus
import de.rki.coronawarnapp.bugreporting.censors.BugCensor.Companion.toNullIfUnmodified
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
    fun `censor string is nulled if not modified`() {
        BugCensor.CensoredString("abc", 1..2).toNullIfUnmodified() shouldNotBe null
        BugCensor.CensoredString("abc", null).toNullIfUnmodified() shouldBe null
    }

    @Test
    fun `censoring range determination`() {
        val input = "1234567890ABCDEFG"
        val one = BugCensor.CensoredString(input, null)

        one.censor("1234", "")!!.apply {
            string shouldBe "567890ABCDEFG"
            range shouldBe 0..4
        }

        one.censor("1234", "....")!!.apply {
            string shouldBe "....567890ABCDEFG"
            range shouldBe 0..4
        }

        one.censor("DEFG", "....")!!.apply {
            string shouldBe "1234567890ABC...."
            range shouldBe 13..(13 + 4)
        }

        one.censor("1234567890ABCDEFG", "...")!!.apply {
            string shouldBe "..."
            range shouldBe 0..(0 + 17)
        }

        one.censor("#", "...") shouldBe null

        one.censor("1234567890ABCDEFG", "1234567890ABCDEFG###")!!.apply {
            string shouldBe "1234567890ABCDEFG###"
            range shouldBe 0..(0 + 17)
        }

        one.censor("", " ")!!.apply {
            string shouldBe " 1 2 3 4 5 6 7 8 9 0 A B C D E F G "
            range shouldBe 0..16
        }
    }

    @Test
    fun `censoring range combination`() {
        (BugCensor.CensoredString("abc", 1..2) + BugCensor.CensoredString("abc", 1..2)).apply {
            string shouldBe "abc"
            range shouldBe 1..2
        }

        (BugCensor.CensoredString("abc", 0..2) + BugCensor.CensoredString("abc", 1..2)).apply {
            range shouldBe 0..2
        }
        (BugCensor.CensoredString("abc", 0..3) + BugCensor.CensoredString("abc", 1..2)).apply {
            range shouldBe 0..3
        }
        (BugCensor.CensoredString("abc", 1..2) + BugCensor.CensoredString("abc", 0..2)).apply {
            range shouldBe 0..2
        }
        (BugCensor.CensoredString("abc", 1..2) + BugCensor.CensoredString("abc", 0..3)).apply {
            range shouldBe 0..3
        }

        (BugCensor.CensoredString("abc", 1..2) + BugCensor.CensoredString("abc", 3..4)).apply {
            range shouldBe 1..4
        }
        (BugCensor.CensoredString("abc", 1..2) + BugCensor.CensoredString("abc", 4..5)).apply {
            range shouldBe 1..5
        }
        (BugCensor.CensoredString("abc", 1..1) + BugCensor.CensoredString("abc", 2..2)).apply {
            range shouldBe 1..2
        }

        (BugCensor.CensoredString("abc", 1..2) + BugCensor.CensoredString("abc", 0..2)).apply {
            range shouldBe 0..2
        }
        (BugCensor.CensoredString("abc", 1..2) + BugCensor.CensoredString("abc", 0..3)).apply {
            range shouldBe 0..3
        }
        (BugCensor.CensoredString("abc", 0..2) + BugCensor.CensoredString("abc", 1..2)).apply {
            range shouldBe 0..2
        }
        (BugCensor.CensoredString("abc", 0..3) + BugCensor.CensoredString("abc", 1..2)).apply {
            range shouldBe 0..3
        }

        (BugCensor.CensoredString("abc", 3..4) + BugCensor.CensoredString("abc", 1..2)).apply {
            range shouldBe 1..4
        }
        (BugCensor.CensoredString("abc", 4..5) + BugCensor.CensoredString("abc", 1..2)).apply {
            range shouldBe 1..5
        }
        (BugCensor.CensoredString("abc", 2..2) + BugCensor.CensoredString("abc", 1..1)).apply {
            range shouldBe 1..2
        }
    }
}
