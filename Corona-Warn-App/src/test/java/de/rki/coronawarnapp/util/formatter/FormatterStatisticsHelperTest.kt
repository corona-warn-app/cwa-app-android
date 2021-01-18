package de.rki.coronawarnapp.util.formatter

import android.content.Context
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.util.getLocale
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.Locale

class FormatterStatisticsHelperTest {

    @MockK
    private lateinit var context: Context

    private val today = LocalDate()
    private val yesterday = LocalDate().minusDays(1)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic("de.rki.coronawarnapp.contactdiary.util.ContactDiaryExtensionsKt")
        with(context) {
            every { context.getLocale() } returns Locale.GERMANY
            every { getString(R.string.statistics_primary_value_today) } returns R.string.statistics_primary_value_today.toString()
            every { getString(R.string.statistics_primary_value_yesterday) } returns R.string.statistics_primary_value_yesterday.toString()
            every { getString(R.string.statistics_primary_value_until_today) } returns R.string.statistics_primary_value_until_today.toString()
            every { getString(R.string.statistics_primary_value_until_yesterday) } returns R.string.statistics_primary_value_until_yesterday.toString()
        }
    }

    @Test
    fun `formatStatisticsDate test`() {
        formatStatisticsDate(context = context,localDate = today) shouldBe context.getString(R.string.statistics_primary_value_today)
        formatStatisticsDate(context = context,localDate = yesterday) shouldBe (context.getString(R.string.statistics_primary_value_yesterday))
        formatStatisticsDate(context = context,localDate = LocalDate("2021-01-02")) shouldBe "Samstag, 02.01.21"
    }

    @Test
    fun `formatStatisticsDateInterval test`() {
        formatStatisticsDateInterval(context = context,localDate = today) shouldBe context.getString(R.string.statistics_primary_value_until_today)
        formatStatisticsDateInterval(context = context,localDate = yesterday) shouldBe (context.getString(R.string.statistics_primary_value_until_yesterday))
        formatStatisticsDateInterval(context = context,localDate = LocalDate("2021-01-02")) shouldBe "Samstag, 02.01.21"
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }
}
