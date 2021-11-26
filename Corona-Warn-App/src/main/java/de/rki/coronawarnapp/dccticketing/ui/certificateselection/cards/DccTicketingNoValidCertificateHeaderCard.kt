package de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DccTicketingNoValidCertificateHeaderCardBinding
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateItem
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class DccTicketingNoValidCertificateHeaderCard(parent: ViewGroup) :
    DccTicketingCertificateSelectionAdapter.CertificatesItemVH<
        DccTicketingNoValidCertificateHeaderCard.Item,
        DccTicketingNoValidCertificateHeaderCardBinding>(
        layoutRes = R.layout.dcc_ticketing_no_valid_certificate_header_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<DccTicketingNoValidCertificateHeaderCardBinding> = lazy {
        DccTicketingNoValidCertificateHeaderCardBinding.bind(itemView)
    }

    override val onBindData: DccTicketingNoValidCertificateHeaderCardBinding.(item: Item, payloads: List<Any>) -> Unit =
        { item, payloads ->
        }

    data class Item(
        val validationCondition: DccTicketingValidationCondition?
    ) : DccTicketingCertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = Item::class.hashCode().toLong()
    }
}
