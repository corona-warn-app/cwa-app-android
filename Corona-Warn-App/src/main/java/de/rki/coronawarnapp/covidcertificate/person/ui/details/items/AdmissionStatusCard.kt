package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.databinding.ConfirmedStatusCardBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class AdmissionStatusCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<AdmissionStatusCard.Item, ConfirmedStatusCardBinding>(
        layoutRes = R.layout.confirmed_status_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<ConfirmedStatusCardBinding> = lazy {
        ConfirmedStatusCardBinding.bind(itemView)
    }

    override val onBindData: ConfirmedStatusCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item

        title.text = curItem.titleText
        subtitle.text = curItem.subtitleText
        badge.isVisible = curItem.badgeText.isNotBlank()
        badge.text = curItem.badgeText
        body.text = curItem.longText
        badge.background = context.getDrawableCompat(item.colorShade.admissionBadgeBg)
        faq.isVisible = curItem.faqAnchor != null
        curItem.faqAnchor?.let { url ->
            faq.convertToHyperlink(url)
        }
    }

    data class Item(
        val titleText: String,
        val subtitleText: String,
        val badgeText: String,
        val longText: String,
        val faqAnchor: String?,
        val colorShade: PersonColorShade,
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = Item::class.hashCode().toLong()
    }
}
