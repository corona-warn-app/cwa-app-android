package de.rki.coronawarnapp.dccticketing.ui.certificateselection.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.DccTicketingNoValidCertificateCardBinding
import de.rki.coronawarnapp.dccticketing.core.transaction.DccTicketingValidationCondition
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateItem
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.DccTicketingCertificateSelectionAdapter
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.getFullName
import de.rki.coronawarnapp.dccticketing.ui.certificateselection.requestedCertificateTypes
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
            val validationCondition = curItem.validationCondition
            if (validationCondition != null) {
                certificateTypes.text =
                    validationCondition.type?.let { requestedCertificateTypes(it, context, ",\n") } ?: ""
                birthDate.text = validationCondition.dob?.let {
                    context.getString(R.string.dcc_ticketing_certificate_birthday).format(it)
                } ?: ""
                name.text = getFullName(validationCondition.fnt, validationCondition.gnt)
            }
        }

    data class Item(
        val validationCondition: DccTicketingValidationCondition?
    ) : DccTicketingCertificateItem, HasPayloadDiffer {
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
        override val stableId = Item::class.hashCode().toLong()
    }
}
