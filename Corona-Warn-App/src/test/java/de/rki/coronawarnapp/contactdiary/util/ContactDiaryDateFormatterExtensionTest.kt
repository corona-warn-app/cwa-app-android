package de.rki.coronawarnapp.contactdiary.util

import io.kotest.matchers.shouldBe
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import java.util.Locale

class ContactDiaryDateFormatterExtensionTest {

    @Test
    fun `format bulgarian date`() {
        LocalDate("2021-01-01").toFormattedDay(
            Locale("bg", "BG")
        ) shouldBe "петък, 1.01.21 г."
    }

    @Test
    fun `format german date`() {
        LocalDate("2021-01-02").toFormattedDay(Locale.GERMANY) shouldBe "Samstag, 02.01.21"
    }

    @Test
    fun `format english (us) date`() {
        LocalDate("2021-01-03").toFormattedDay(Locale.US) shouldBe "Sunday, 1/3/21"
    }

    @Test
    fun `format english (uk) date`() {
        LocalDate("2021-01-03").toFormattedDay(Locale.UK) shouldBe "Sunday, 03/01/2021"
    }

    @Test
    fun `format polish date`() {
        LocalDate("2021-01-04").toFormattedDay(
            Locale("pl", "PL")
        ) shouldBe "poniedziałek, 04.01.2021"
    }

    @Test
    fun `format romanian date`() {
        LocalDate("2021-01-05").toFormattedDay(
            Locale("ro", "RO")
        ) shouldBe "marți, 05.01.2021"
    }

    @Test
    fun `format turkish date`() {
        LocalDate("2021-01-06").toFormattedDay(
            Locale("tr", "TR")
        ) shouldBe "Çarşamba, 6.01.2021"
    }
}
