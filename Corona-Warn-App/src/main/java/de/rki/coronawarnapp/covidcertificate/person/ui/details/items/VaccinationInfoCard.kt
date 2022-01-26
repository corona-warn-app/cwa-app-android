package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.databinding.VaccinationInfoCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import setTextWithUrl

class VaccinationInfoCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<VaccinationInfoCard.Item, VaccinationInfoCardBinding>(
        layoutRes = R.layout.vaccination_info_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<VaccinationInfoCardBinding> = lazy {
        VaccinationInfoCardBinding.bind(itemView)
    }

    override val onBindData: VaccinationInfoCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item

        title.text = curItem.titleText
        subtitle.text = curItem.subtitleText
        body.text = curItem.longText

        faq.setTextWithUrl(
            context.resources.getString(R.string.confirmed_status_faq_text),
            context.resources.getString(R.string.confirmed_status_faq_text),
            curItem.faqAnchor
        )
    }

    data class Item(
        val titleText: String,
        val subtitleText: String,
        val longText: String,
        val faqAnchor: String,
    ) : CertificateItem, HasPayloadDiffer {
        override val stableId = Item::class.hashCode().toLong()
    }
}
