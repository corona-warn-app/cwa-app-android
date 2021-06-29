package de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.VaccinationListAdapter
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.VaccinationListItem
import de.rki.coronawarnapp.covidcertificate.vaccination.ui.list.adapter.viewholder.VaccinationListImmunityInformationCardItemVH.VaccinationListImmunityInformationCardItem
import de.rki.coronawarnapp.databinding.VaccinationListImmunityCardBinding

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
        body.text =
            context.resources.getQuantityString(
        val timeTillImmunity = item.timeUntilImmunity
        body.text = if (timeTillImmunity < Duration.standardDays(1)) {
            resources.getString(R.string.vaccination_list_immunity_tomorrow_card_body)
        } else {
            // We round up for nicer UX, despite being on the 1st day of 15 and days left < 15, we show 15 days left.
            // 15, 14, ..., 2, tomorrow, immune.
            val days = timeTillImmunity.standardDays + 1
            resources.getQuantityString(
                R.plurals.vaccination_list_immunity_card_body,
                item.daysUntilImmunity,
                item.daysUntilImmunity
            )
        }
    }

    data class VaccinationListImmunityInformationCardItem(val daysUntilImmunity: Int) : VaccinationListItem {
        override val stableId = VaccinationListImmunityInformationCardItem::class.java.name.hashCode().toLong()
    }
}
