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
            every { getString(R.string.statistics_primary_value_today) } returns TODAY
            every { getString(R.string.statistics_primary_value_yesterday) } returns YESTERDAY
            every { getString(R.string.statistics_primary_value_until_today) } returns UNTIL_TODAY
            every { getString(R.string.statistics_primary_value_until_yesterday) } returns UNTIL_YESTERDAY
        }
    }

    @Test
    fun `formatStatisticsDate test`() {
        formatStatisticsDate(
            context = context,
            localDate = today
        ) shouldBe TODAY
        formatStatisticsDate(
            context = context,
            localDate = yesterday
        ) shouldBe YESTERDAY
        formatStatisticsDate(context = context, localDate = LocalDate("2021-01-02")) shouldBe "Samstag, 02.01.21"
    }

    @Test
    fun `formatStatisticsDateInterval test`() {
        formatStatisticsDateInterval(
            context = context,
            localDate = today
        ) shouldBe UNTIL_TODAY
        formatStatisticsDateInterval(
            context = context,
            localDate = yesterday
        ) shouldBe UNTIL_YESTERDAY
        formatStatisticsDateInterval(
            context = context,
            localDate = LocalDate("2021-01-02")
        ) shouldBe "Samstag, 02.01.21"
    }

    @After
    fun cleanUp() {
        unmockkAll()
    }

    companion object {
        private const val TODAY = "Today"
        private const val YESTERDAY = "Yesterday"
        private const val UNTIL_TODAY = "Until today"
        private const val UNTIL_YESTERDAY = "Until yesterday"
    }
}
