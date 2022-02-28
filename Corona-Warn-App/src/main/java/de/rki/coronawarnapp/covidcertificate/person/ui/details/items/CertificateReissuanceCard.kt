package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.PersonDetailsCertificateReissuanceCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class CertificateReissuanceCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<
        CertificateReissuanceCard.Item,
        PersonDetailsCertificateReissuanceCardBinding
        >(
        layoutRes = R.layout.person_details_certificate_reissuance_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<PersonDetailsCertificateReissuanceCardBinding> = lazy {
        PersonDetailsCertificateReissuanceCardBinding.bind(itemView)
    }

    override val onBindData: PersonDetailsCertificateReissuanceCardBinding.(item: Item, payloads: List<Any>) -> Unit =
        { item, payloads ->
            val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
            root.setOnClickListener { curItem.onClick() }

            reissuanceBadge.isVisible = curItem.badgeVisible
            title.text = curItem.title
            subtitle.text = curItem.subtitle
        }

    data class Item(
        val title: String,
        val subtitle: String,
        val badgeVisible: Boolean,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {

        override val stableId = Item::class.hashCode().toLong()
    }
}
