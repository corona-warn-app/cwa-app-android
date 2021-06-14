package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.core.PersonCertificates
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.covidcertificate.test.ui.items.CertificatesItem
import de.rki.coronawarnapp.databinding.ItemPersonOverviewBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PersonOverviewCard(parent: ViewGroup) :
    PersonOverviewAdapter.PersonOverviewItemVH<PersonOverviewCard.Item, ItemPersonOverviewBinding>(
        R.layout.item_person_overview,
        parent
    ) {

    override val viewBinding = lazy { ItemPersonOverviewBinding.inflate(layoutInflater) }

    override val onBindData: ItemPersonOverviewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item

        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        val personCertificates: PersonCertificates,
        val onClickAction: (Item) -> Unit,
    ) : CertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = personCertificates.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
