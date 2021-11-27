package de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DccTicketingNoValidCertificateFaqCardBinding
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateItem
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionAdapter
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import setTextWithUrl
import java.util.Locale

class DccTicketingNoValidCertificateFaqCard(parent: ViewGroup) :
    DccTicketingCertificateSelectionAdapter.CertificatesItemVH<
        DccTicketingNoValidCertificateFaqCard.Item,
        DccTicketingNoValidCertificateFaqCardBinding>(
        layoutRes = R.layout.dcc_ticketing_no_valid_certificate_faq_card,
        parent = parent
    ) {

    override val viewBinding: Lazy<DccTicketingNoValidCertificateFaqCardBinding> = lazy {
        DccTicketingNoValidCertificateFaqCardBinding.bind(itemView)
    }

    override val onBindData: DccTicketingNoValidCertificateFaqCardBinding.(item: Item, payloads: List<Any>) -> Unit =
        { _, _ ->
            val link = when (Locale.getDefault().language) {
                Locale.GERMAN.language -> R.string.dcc_ticketing_faq_link_german
                else -> R.string.dcc_ticketing_faq_link_english
            }
            faqLink.setTextWithUrl(
                R.string.dcc_ticketing_certificate_selection_more_information_text,
                R.string.dcc_ticketing_faq_link_container,
                link
            )
        }

    class Item : DccTicketingCertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = Item::class.hashCode().toLong()
    }
}
