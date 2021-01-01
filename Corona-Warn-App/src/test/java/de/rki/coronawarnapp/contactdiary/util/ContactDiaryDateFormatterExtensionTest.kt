package de.rki.coronawarnapp.contactdiary.util

import org.joda.time.LocalDate
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.jupiter.api.Test
import java.util.Locale

class ContactDiaryDateFormatterExtensionTest {
    private lateinit var initialDefaultLocale: Locale

    @BeforeClass
    fun setup() {
        initialDefaultLocale = Locale.getDefault()
    }

    @AfterClass
    fun teardown() {
        // Reset the locale after all tests were executed to make sure
        // other tests will run correctly
        Locale.setDefault(initialDefaultLocale)
    }

    @Test
    fun `format bulgarian date`() {
        Locale.setDefault(Locale("bg", "BG"))

        val testDate = LocalDate("2021-01-01")

        Assert.assertEquals("Петък, 01.01.21", testDate.toFormattedDay())
    }

    @Test
    fun `format german date`() {
        Locale.setDefault(Locale.GERMANY)

        val testDate = LocalDate("2021-01-02")

        Assert.assertEquals("Samstag, 02.01.21", testDate.toFormattedDay())
    }

    @Test
    fun `format english date`() {
        Locale.setDefault(Locale.US)

        val testDate = LocalDate("2021-01-03")

        Assert.assertEquals("Sunday, 1/3/21", testDate.toFormattedDay())
    }

    @Test
    fun `format polish date`() {
        Locale.setDefault(Locale("pl", "PL"))

        val testDate = LocalDate("2021-01-04")

        Assert.assertEquals("poniedziałek, 04.01.21", testDate.toFormattedDay())
    }

    @Test
    fun `format romanian date`() {
        Locale.setDefault(Locale("ro", "RO"))

        val testDate = LocalDate("2021-01-05")

        Assert.assertEquals("marţi, 05.01.2021", testDate.toFormattedDay())
    }

    @Test
    fun `format turkish date`() {
        Locale.setDefault(Locale("tr", "TR"))

        val testDate = LocalDate("2021-01-06")

        Assert.assertEquals("Çarşamba, 06.01.2021", testDate.toFormattedDay())
    }
}
