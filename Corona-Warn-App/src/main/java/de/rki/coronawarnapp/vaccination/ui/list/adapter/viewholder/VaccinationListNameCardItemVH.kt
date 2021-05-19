package de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListNameCardBinding
import de.rki.coronawarnapp.util.list.Movable
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListNameCardItemVH.VaccinationListNameCardItem

class VaccinationListNameCardItemVH(parent: ViewGroup) :
    VaccinationListAdapter.ItemVH<VaccinationListNameCardItem, VaccinationListNameCardBinding>(
        layoutRes = R.layout.vaccination_list_name_card,
        parent = parent
    ),
    Movable {

    override val viewBinding: Lazy<VaccinationListNameCardBinding> = lazy {
        VaccinationListNameCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationListNameCardBinding
    .(item: VaccinationListNameCardItem, payloads: List<Any>) -> Unit =
        { item, _ ->
            nameCardTitle.text = item.fullName
            nameCardSubtitle.text = context.getString(
                R.string.vaccination_list_name_card_subtitle,
                item.dayOfBirth
            )
        }

    data class VaccinationListNameCardItem(val fullName: String, val dayOfBirth: String) : VaccinationListItem {
        override val stableId = this.hashCode().toLong()
    }
}
