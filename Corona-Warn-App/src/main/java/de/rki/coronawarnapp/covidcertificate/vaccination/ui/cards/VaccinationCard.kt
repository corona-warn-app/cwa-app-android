package de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.CertificatesAdapter
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.databinding.VaccinationHomeCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

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
            }
            else -> {
                vaccinationState.setText(R.string.vaccination_card_status_vaccination_incomplete)
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
