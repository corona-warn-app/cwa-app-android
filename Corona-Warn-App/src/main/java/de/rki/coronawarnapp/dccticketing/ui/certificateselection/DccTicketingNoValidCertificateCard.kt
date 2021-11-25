package de.rki.coronawarnapp.dccticketing.ui.certificateselection

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DccTicketingNoValidCertificateCardBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class DccTicketingNoValidCertificateCard(parent: ViewGroup) :
    DccTicketingCertificateSelectionAdapter.CertificatesItemVH<
        DccTicketingNoValidCertificateCard.Item,
        DccTicketingNoValidCertificateCardBinding>(
        layoutRes = R.layout.dcc_ticketing_no_valid_certificate_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<DccTicketingNoValidCertificateCardBinding> = lazy {
        DccTicketingNoValidCertificateCardBinding.bind(itemView)
    }

    override val onBindData: DccTicketingNoValidCertificateCardBinding.(item: Item, payloads: List<Any>) -> Unit =
        { item, payloads ->
            val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        }

    object Item : DccTicketingCertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = 30L
    }
}
