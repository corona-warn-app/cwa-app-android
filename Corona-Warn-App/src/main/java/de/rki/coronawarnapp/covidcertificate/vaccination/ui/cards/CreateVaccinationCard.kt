package de.rki.coronawarnapp.covidcertificate.vaccination.ui.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.test.ui.CertificatesAdapter
import de.rki.coronawarnapp.covidcertificate.test.ui.items.CertificatesItem
import de.rki.coronawarnapp.databinding.VaccinationHomeRegistrationCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

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

    data class Item(val onClickAction: (Item) -> Unit) : CertificatesItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
