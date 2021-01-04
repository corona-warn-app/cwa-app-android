package de.rki.coronawarnapp.contactdiary.util

import de.rki.coronawarnapp.util.device.DefaultSystemInfoProvider
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

class ContactDiaryDateFormatterExtensionTest {
    @MockK lateinit var defaultSystemInfoProvider: DefaultSystemInfoProvider

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Test
    fun `format bulgarian date`() {
        every { defaultSystemInfoProvider.locale } returns Locale("bg", "BG")

        val testDate = LocalDate("2021-01-01")

        Assert.assertEquals("петък, 1.01.21 г.", testDate.toFormattedDay(defaultSystemInfoProvider))
    }

    @Test
    fun `format german date`() {
        every { defaultSystemInfoProvider.locale } returns Locale.GERMANY

        val testDate = LocalDate("2021-01-02")

        Assert.assertEquals("Samstag, 02.01.21", testDate.toFormattedDay(defaultSystemInfoProvider))
    }

    @Test
    fun `format english (us) date`() {
        every { defaultSystemInfoProvider.locale } returns Locale.US

        val testDate = LocalDate("2021-01-03")

        Assert.assertEquals("Sunday, 1/3/21", testDate.toFormattedDay(defaultSystemInfoProvider))
    }

    @Test
    fun `format english (uk) date`() {
        every { defaultSystemInfoProvider.locale } returns Locale.UK

        val testDate = LocalDate("2021-01-03")

        Assert.assertEquals("Sunday, 03/01/2021", testDate.toFormattedDay(defaultSystemInfoProvider))
    }

    @Test
    fun `format polish date`() {
        every { defaultSystemInfoProvider.locale } returns Locale("pl", "PL")

        val testDate = LocalDate("2021-01-04")

        Assert.assertEquals("poniedziałek, 04.01.2021", testDate.toFormattedDay(defaultSystemInfoProvider))
    }

    @Test
    fun `format romanian date`() {
        every { defaultSystemInfoProvider.locale } returns Locale("ro", "RO")

        val testDate = LocalDate("2021-01-05")

        Assert.assertEquals("marți, 05.01.2021", testDate.toFormattedDay(defaultSystemInfoProvider))
    }

    @Test
    fun `format turkish date`() {
        every { defaultSystemInfoProvider.locale } returns Locale("tr", "TR")

        val testDate = LocalDate("2021-01-06")

        Assert.assertEquals("Çarşamba, 6.01.2021", testDate.toFormattedDay(defaultSystemInfoProvider))
    }
}
