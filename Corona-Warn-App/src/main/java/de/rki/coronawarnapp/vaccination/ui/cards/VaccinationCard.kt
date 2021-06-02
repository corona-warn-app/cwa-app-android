package de.rki.coronawarnapp.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationHomeCardBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.vaccination.core.VaccinatedPerson

class VaccinationCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<VaccinationCard.Item, VaccinationHomeCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        VaccinationHomeCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: VaccinationHomeCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        personName.text = curItem.vaccinatedPerson.fullName
        when (curItem.vaccinatedPerson.getVaccinationStatus()) {
            VaccinatedPerson.Status.COMPLETE -> {
                val days = curItem.vaccinatedPerson.getTimeUntilImmunity()!!.standardDays.toInt()
                vaccinationState.text = context.resources.getQuantityString(
                    R.plurals.vaccination_card_status_vaccination_complete,
                    days,
                    days
                )
                icon.setImageResource(R.drawable.vaccination_card_icon_complete)
            }
            else -> {
                vaccinationState.setText(R.string.vaccination_card_status_vaccination_incomplete)
                icon.setImageResource(R.drawable.vaccination_card_icon_incomplete)
            }
        }
        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        override val vaccinatedPerson: VaccinatedPerson,
        val onClickAction: (Item) -> Unit,
    ) : VaccinationStatusItem, HasPayloadDiffer {

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
