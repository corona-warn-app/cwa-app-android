package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.annotation.SuppressLint
import android.view.ViewGroup
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

    @SuppressLint("SetTextI18n")
    override val onBindData: PersonDetailsCertificateReissuanceCardBinding.(item: Item, payloads: List<Any>) -> Unit =
        { curItem, payloads ->
            root.setOnClickListener { curItem.onClick() }
            // TODO
        }

    data class Item(
        val title: String,
        val subtitle: String,
        val onClick: () -> Unit
    ) : CertificateItem, HasPayloadDiffer {

        override val stableId = Item::class.hashCode().toLong()
    }
}
