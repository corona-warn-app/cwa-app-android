package de.rki.coronawarnapp.contactdiary.util

import io.kotest.matchers.collections.shouldBeIn
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Locale

class ContactDiaryDateFormatterExtensionTest {

    @Test
    fun `format bulgarian date`() {
        LocalDate.parse("2021-01-01").toFormattedDay(
            Locale("bg", "BG")
        ) shouldBeIn arrayOf("петък, 1.01.21 г.", "Петък, 1.01.21 г.")
    }

    @Test
    fun `format german date`() {
        LocalDate.parse("2021-01-02").toFormattedDay(Locale.GERMANY) shouldBe "Samstag, 02.01.21"
    }

    @Test
    fun `format english (us) date`() {
        LocalDate.parse("2021-01-03").toFormattedDay(Locale.US) shouldBe "Sunday, 1/3/21"
    }

    @Test
    fun `format english (uk) date`() {
        LocalDate.parse("2021-01-03").toFormattedDay(Locale.UK) shouldBe "Sunday, 03/01/2021"
    }

    @Test
    fun `format polish date`() {
        LocalDate.parse("2021-01-04").toFormattedDay(
            Locale("pl", "PL")
        ) shouldBe "poniedziałek, 04.01.2021"
    }

    @Test
    fun `format romanian date`() {
        LocalDate.parse("2021-01-05").toFormattedDay(
            Locale("ro", "RO")
        ) shouldBeIn arrayOf("marţi, 05.01.2021", "marți, 05.01.2021")
    }

    @Test
    fun `format turkish date`() {
        LocalDate.parse("2021-01-06").toFormattedDay(
            Locale("tr", "TR")
        ) shouldBe "Çarşamba, 6.01.2021"
    }

    @Test
    fun `format ukrainian date`() {
        LocalDate.parse("2021-01-06").toFormattedDay(
            Locale("uk", "UA")
        ) shouldBe "середа, 06.01.21"
    }
}
