package de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.DiaryOverviewAdapter
import de.rki.coronawarnapp.databinding.ContactDiaryOverviewListSubheaderBinding

class OverviewSubHeaderVH(parent: ViewGroup) :
    DiaryOverviewAdapter.ItemVH<OverviewSubHeaderItem, ContactDiaryOverviewListSubheaderBinding>(
        layoutRes = R.layout.contact_diary_overview_list_subheader,
        parent = parent
    ) {

    override val viewBinding: Lazy<ContactDiaryOverviewListSubheaderBinding> =
        lazy { ContactDiaryOverviewListSubheaderBinding.bind(itemView) }

    override val onBindData: ContactDiaryOverviewListSubheaderBinding.(
        item: OverviewSubHeaderItem,
        payloads: List<Any>
    ) -> Unit = { _, _ ->
        // NOOP
    }
}
