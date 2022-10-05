package de.rki.coronawarnapp.submission.ui.homecards

import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.toLocalDateTimeUserTz
import java.time.Instant
import java.time.format.DateTimeFormatter

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

    fun Instant.formatAsUserTestRegisteredAt(): String =
        toLocalDateTimeUserTz().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yy"))
}
