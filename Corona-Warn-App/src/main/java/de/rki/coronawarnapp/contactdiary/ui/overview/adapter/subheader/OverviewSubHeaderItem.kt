package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader

import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewItem

object OverviewSubHeaderItem : DiaryOverviewItem {

    override val stableId: Long = this.hashCode().toLong()
}
