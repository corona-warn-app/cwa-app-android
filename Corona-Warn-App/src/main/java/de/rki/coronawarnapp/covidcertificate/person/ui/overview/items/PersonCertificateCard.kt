package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.graphics.Bitmap
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.databinding.ItemPersonOverviewBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class PersonCertificateCard(parent: ViewGroup) :
    PersonOverviewAdapter.PersonOverviewItemVH<PersonCertificateCard.Item, ItemPersonOverviewBinding>(
        R.layout.item_person_overview,
        parent
    ) {

    override val viewBinding = lazy { ItemPersonOverviewBinding.inflate(layoutInflater) }

    override val onBindData: ItemPersonOverviewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        name.text = item.certificate.run {
            buildString {
                if (!firstName.isNullOrBlank()) append("$firstName ")
                append(lastName)
            }
        }
        qrcodeImage.setImageBitmap(item.qrcodeBitmap)
        itemView.setOnClickListener { curItem.onClickAction(item) }
    }

    data class Item(
        val certificate: CwaCovidCertificate,
        val qrcodeBitmap: Bitmap?,
        val onClickAction: (Item) -> Unit,
    ) : CertificatesItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = certificate.personIdentifier.codeSHA256.hashCode().toLong()
    }
}
