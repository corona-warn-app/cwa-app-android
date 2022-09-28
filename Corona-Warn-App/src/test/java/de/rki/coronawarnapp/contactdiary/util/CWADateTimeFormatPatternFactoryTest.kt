package de.rki.coronawarnapp.contactdiary.util

import de.rki.coronawarnapp.contactdiary.util.CWADateTimeFormatPatternFactory.shortDatePattern
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class CWADateTimeFormatPatternFactoryTest {

    @Test
    fun `pattern for german date`() {
        Locale.GERMANY.shortDatePattern() shouldBe "dd.MM.yy"
    }

    @Test
    fun `pattern for bulgarian date`() {
        Locale("bg", "BG").shortDatePattern() shouldBe "d.MM.yy 'г'."
        LocalDate.now()
            .format(DateTimeFormatter.ofPattern("d.MM.yy 'г'.").withLocale(Locale("bg", "BG"))) shouldBe LocalDate.now()
            .format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(Locale("bg", "BG"))
            )
    }

    @Test
    fun `pattern for gb date`() {
        Locale.UK.shortDatePattern() shouldBe "dd/MM/yyyy"
    }

    @Test
    fun `pattern for us date`() {
        Locale.US.shortDatePattern() shouldBe "M/d/yy"
    }

    @Test
    fun `pattern for romanian date`() {
        Locale("ro", "RO").shortDatePattern() shouldBe "dd.MM.yyyy"
    }

    @Test
    fun `pattern for ukrainian  date`() {
        Locale("uk", "UA").shortDatePattern() shouldBe "dd.MM.yyyy"
    }

    @Test
    fun `pattern for polish date`() {
        Locale("pl", "PL").shortDatePattern() shouldBe "dd.MM.yyyy"
    }

    @Test
    fun `pattern for turkish date`() {
        Locale("tr", "TR").shortDatePattern() shouldBe "d.MM.yyyy"
    }
}
