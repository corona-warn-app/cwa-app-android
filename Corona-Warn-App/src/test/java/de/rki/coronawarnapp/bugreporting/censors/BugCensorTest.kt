package de.rki.coronawarnapp.bugreporting.censors

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
        BugCensor.CensorContainer("abc").compile() shouldBe null
        BugCensor.CensorContainer("abc").censor("abc", "123").compile() shouldNotBe null
        BugCensor.CensorContainer("abc").censor("123", "abc").compile() shouldBe null
    }

    @Test
    fun `censoring range determination`() {
        val input = "1234567890ABCDEFG"
        val one = BugCensor.CensorContainer(input)

        one.censor("1234", "").apply {
            actions.single().apply {
                range shouldBe 0..4
                execute(original) shouldBe "567890ABCDEFG"
            }
        }

        one.censor("1234", "....").apply {
            actions.single().apply {
                execute(original) shouldBe "....567890ABCDEFG"
                range shouldBe 0..4
            }
        }

        one.censor("DEFG", "....").apply {
            actions.single().apply {
                execute(original) shouldBe "1234567890ABC...."
                range shouldBe 13..(13 + 4)
            }
        }

        one.censor("1234567890ABCDEFG", "...").apply {
            actions.single().apply {
                execute(original) shouldBe "..."
                range shouldBe 0..(0 + 17)
            }
        }

        one.censor("#", "...").actions shouldBe emptySet()

        one.censor("1234567890ABCDEFG", "1234567890ABCDEFG###").apply {
            actions.single().apply {
                execute(original) shouldBe "1234567890ABCDEFG###"
                range shouldBe 0..(0 + 17)
            }
        }

        one.censor("", " ").apply {
            actions.single().apply {
                execute(original) shouldBe " 1 2 3 4 5 6 7 8 9 0 A B C D E F G "
                range shouldBe 0..16
            }
        }
    }

    @Test
    fun `censoring range combination`() {
        val container1 = BugCensor.CensorContainer("abcdefg")
        container1.actions shouldBe emptySet()
        val container2 = container1.censor("cde", "345")
        container2.actions.map { it.range }.toSet() shouldBe setOf(2..5)
        val container3 = container2.censor("ab", "12")
        container3.actions.map { it.range }.toSet() shouldBe setOf(0..2, 2..5)
    }

    @Test
    fun `censoring disjoint`() {
        BugCensor.CensorContainer("#abcdefg*")
            .censor("abc", "123")
            .censor("efg", "567")
            .compile()!!
            .apply {
                censored shouldBe "#123d567*"
                ranges shouldBe setOf(1..4, 5..8)
            }
    }

    @Test
    fun `censoring disjoint - touching`() {
        BugCensor.CensorContainer("#abcefg*")
            .censor("abc", "123")
            .censor("efg", "567")
            .compile()!!
            .apply {
                censored shouldBe "#123567*"
                ranges shouldBe setOf(1..4, 4..7)
            }
    }

    @Test
    fun `censoring overlap`() {
        BugCensor.CensorContainer("#abcdefg*")
            .censor("abcd", "1234")
            .censor("defg", "4567")
            .compile()!!
            .apply {
                censored shouldBe "#<censor-collision/>*"
                ranges shouldBe setOf(1..8)
            }
    }

    @Test
    fun `censoring complete overlap`() {
        BugCensor.CensorContainer("#abcdefg*")
            .censor("abc", "---")
            .censor("abc", "+++")
            .compile()!!
            .apply {
                censored shouldBe "#<censor-collision/>defg*"
                ranges shouldBe setOf(1..4, 1..4)
            }
    }

    @Test
    fun `full replacement collision`() {
        BugCensor.CensorContainer("#abcdefg*")
            .censor("#abcdefg*", "#1234567*")
            .censor("#abcdefg*", "#*")
            .compile()!!
            .apply {
                censored shouldBe "<censor-collision/>"
                ranges shouldBe setOf(0..9, 0..9)
            }
    }

    @Test
    fun `nested replacement collision`() {
        BugCensor.CensorContainer("#abcdefg*")
            .censor("#abcdefg*", "#abcdefg*")
            .censor("abcdefg", "abcdefg")
            .compile()!!
            .apply {
                censored shouldBe "<censor-collision/>"
                ranges shouldBe setOf(0..9)
            }
    }

    @Test
    fun `string length boundary check`() {
        BugCensor.CensorContainer("#abcdefg*")
            .censor("*", "**")
            .compile()!!
            .apply {
                censored shouldBe "#abcdefg**"
                ranges shouldBe setOf(8..9)
            }
    }
}
