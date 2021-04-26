package de.rki.coronawarnapp.submission.ui.homecards

import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import org.joda.time.Instant

interface TestResultItem : HomeItem {
    override val stableId: Long
        get() = TestResultItem::class.java.name.hashCode().toLong()

    interface RA : TestResultItem {
        override val stableId: Long
            get() = LIST_ID

        companion object {
            val LIST_ID = RA::class.java.name.hashCode().toLong()
        }
    }

    interface PCR : TestResultItem {
        override val stableId: Long
            get() = LIST_ID

        companion object {
            val LIST_ID = PCR::class.java.name.hashCode().toLong()
        }
    }

    fun Instant.formatAsUserTestRegisteredAt(): String = toUserTimeZone().toLocalDate().toString("dd.MM.yy")
}
