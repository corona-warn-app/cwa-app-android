package de.rki.coronawarnapp.familytest.ui.testlist.items

import de.rki.coronawarnapp.util.TimeAndDateExtensions.toUserTimeZone
import de.rki.coronawarnapp.util.lists.HasStableId
import org.joda.time.Instant

interface FamilyTestListItem : HasStableId {
    override val stableId: Long
        get() = FamilyTestListItem::class.java.name.hashCode().toLong()

    interface RA : FamilyTestListItem {
        override val stableId: Long
            get() = LIST_ID

        companion object {
            val LIST_ID = RA::class.java.name.hashCode().toLong()
        }
    }

    interface PCR : FamilyTestListItem {
        override val stableId: Long
            get() = LIST_ID

        companion object {
            val LIST_ID = PCR::class.java.name.hashCode().toLong()
        }
    }

    fun Instant.formatAsUserTestRegisteredAt(): String = toUserTimeZone().toLocalDate().toString("dd.MM.yy")
}
