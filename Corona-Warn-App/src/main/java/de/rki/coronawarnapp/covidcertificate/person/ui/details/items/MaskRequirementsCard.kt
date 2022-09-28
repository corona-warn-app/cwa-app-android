package de.rki.coronawarnapp.covidcertificate.person.ui.details.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.MaskState
import de.rki.coronawarnapp.covidcertificate.person.ui.details.PersonDetailsAdapter
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonColorShade
import de.rki.coronawarnapp.databinding.MaskRequirementsCardBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.convertToHyperlink
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class MaskRequirementsCard(parent: ViewGroup) :
    PersonDetailsAdapter.PersonDetailsItemVH<MaskRequirementsCard.Item, MaskRequirementsCardBinding>(
        layoutRes = R.layout.mask_requirements_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<MaskRequirementsCardBinding> = lazy {
        MaskRequirementsCardBinding.bind(itemView)
    }

    override val onBindData: MaskRequirementsCardBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item

        maskReqTitle.text = curItem.titleText
        maskReqSubtitle.text = curItem.subtitleText
        maskReqBody.text = curItem.longText
        when (curItem.maskStateIdentifier) {
            MaskState.MaskStateIdentifier.REQUIRED,
            MaskState.MaskStateIdentifier.OPTIONAL -> {
                maskReqBadge.isVisible = true
                maskReqBadge.background = context.getDrawableCompat(curItem.colorShade.maskSmallBadge)
            }
            else -> maskReqBadge.isVisible = false
        }
        maskReqFaq.isVisible = curItem.faqAnchor != null
        curItem.faqAnchor?.let { url ->
            maskReqFaq.convertToHyperlink(url)
        }
    }

    data class Item(
        val titleText: String,
        val subtitleText: String,
        val maskStateIdentifier: MaskState.MaskStateIdentifier?,
        val longText: String,
        val faqAnchor: String?,
        val colorShade: PersonColorShade
    ) : CertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId: Long = Item::class.hashCode().toLong()
    }
}
