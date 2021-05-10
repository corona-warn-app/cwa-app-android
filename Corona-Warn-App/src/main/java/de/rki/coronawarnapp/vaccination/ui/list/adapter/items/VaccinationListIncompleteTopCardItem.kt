package de.rki.coronawarnapp.vaccination.ui.list.adapter.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListIncompleteTopCardBinding
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.items.VaccinationListIncompleteTopCardItemVH.VaccinationListIncompleteTopCardItem

class VaccinationListIncompleteTopCardItemVH(parent: ViewGroup) :
    VaccinationListAdapter.ItemVH<VaccinationListIncompleteTopCardItem, VaccinationListIncompleteTopCardBinding>(
        layoutRes = R.layout.vaccination_list_incomplete_top_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationListIncompleteTopCardBinding> = lazy {
        VaccinationListIncompleteTopCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationListIncompleteTopCardBinding
    .(item: VaccinationListIncompleteTopCardItem, payloads: List<Any>) -> Unit = { _, _ -> // NOOP
    }

    object VaccinationListIncompleteTopCardItem : VaccinationListItem {
        override val stableId = this.hashCode().toLong()
    }
}
