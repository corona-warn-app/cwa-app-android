package de.rki.coronawarnapp.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.VaccinationHomeRegistrationCardBinding
import de.rki.coronawarnapp.greencertificate.ui.certificates.CertificatesAdapter
import de.rki.coronawarnapp.greencertificate.ui.certificates.items.CertificatesItem

class CreateVaccinationCard(parent: ViewGroup) :
    CertificatesAdapter.CertificatesItemVH<CreateVaccinationCard.Item, VaccinationHomeRegistrationCardBinding>(
        R.layout.home_card_container_layout,
        parent
    ) {

    override val viewBinding = lazy {
        VaccinationHomeRegistrationCardBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: VaccinationHomeRegistrationCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->

        fun onClick() {
            val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
            curItem.onClickAction(item)
        }

        itemView.setOnClickListener {
            onClick()
        }
    }

    data class Item(val onClickAction: (Item) -> Unit) : CertificatesItem {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()
    }
}
