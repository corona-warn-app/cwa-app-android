package de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinatedPerson
import de.rki.coronawarnapp.databinding.VaccinationHomeImmuneCardBinding
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toDayFormat
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class ImmuneVaccinationCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<ImmuneVaccinationCard.Item, VaccinationHomeImmuneCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        VaccinationHomeImmuneCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: VaccinationHomeImmuneCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        personName.text = curItem.vaccinatedPerson.fullName
        vaccinationState.text = context.getString(
            R.string.vaccination_card_status_vaccination_complete,
            curItem.vaccinatedPerson.getMostRecentVaccinationCertificate.expiresAt.toDayFormat()
        )

        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        override val vaccinatedPerson: VaccinatedPerson,
        val onClickAction: (Item) -> Unit,
    ) : VaccinationStatusItem, HasPayloadDiffer {

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
