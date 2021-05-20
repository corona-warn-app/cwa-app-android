package de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationListImmunityCardBinding
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.vaccination.ui.list.adapter.viewholder.VaccinationListImmunityInformationCardItemVH.VaccinationListImmunityInformationCardItem
import org.joda.time.Duration

class VaccinationListImmunityInformationCardItemVH(parent: ViewGroup) :
    VaccinationListAdapter.ItemVH<VaccinationListImmunityInformationCardItem, VaccinationListImmunityCardBinding>(
        layoutRes = R.layout.vaccination_list_immunity_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationListImmunityCardBinding> = lazy {
        VaccinationListImmunityCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationListImmunityCardBinding
    .(item: VaccinationListImmunityInformationCardItem, payloads: List<Any>) -> Unit = { item, _ ->
        val daysUntilImmunity = item.timeUntilImmunity.standardDays.toInt()
        body.text =
            context.resources.getQuantityString(
                R.plurals.vaccination_list_immunity_card_body,
                daysUntilImmunity,
                daysUntilImmunity
            )
    }

    data class VaccinationListImmunityInformationCardItem(val timeUntilImmunity: Duration) : VaccinationListItem {
        override val stableId = VaccinationListImmunityInformationCardItem::class.java.name.hashCode().toLong()
    }
}
