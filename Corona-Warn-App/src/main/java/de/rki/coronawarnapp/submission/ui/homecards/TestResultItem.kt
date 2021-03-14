package de.rki.coronawarnapp.submission.ui.homecards

import de.rki.coronawarnapp.ui.main.home.items.HomeItem

interface TestResultItem : HomeItem {
    override val stableId: Long
        get() = TestResultItem::class.java.name.hashCode().toLong()
}
