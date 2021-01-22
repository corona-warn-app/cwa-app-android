package de.rki.coronawarnapp.statistics.ui.homecards

import de.rki.coronawarnapp.statistics.StatisticsData
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import org.junit.Test

internal class StatisticsHomeCardItemTest {

    @Test
    fun `test equals() of statistics item should return true when onHelpAction click listener is different`() {

        val itemWithClickListener1 = StatisticsHomeCard.Item(StatisticsData(emptyList())) {
            // ClickListener1
        }

        val itemWithClickListener2 = StatisticsHomeCard.Item(StatisticsData(emptyList())) {
            // ClickListener2
        }

        // Check if click listeners are actually different
        if (itemWithClickListener1.onHelpAction == itemWithClickListener2.onHelpAction) {
            fail("Different Click Listeners should be set to StatisticsHomeCard.Item")
        }

        // basic assertion
        (itemWithClickListener1 == itemWithClickListener2) shouldBe true

        // assert symmetry
        (itemWithClickListener2 == itemWithClickListener1) shouldBe true

        // assert reflexivity
        (itemWithClickListener1 == itemWithClickListener1) shouldBe true
    }
}
