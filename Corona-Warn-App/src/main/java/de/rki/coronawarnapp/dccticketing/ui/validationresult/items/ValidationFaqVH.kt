package de.rki.coronawarnapp.dccticketing.ui.validationresult.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultFaqItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import setTextWithUrl

class ValidationFaqVH(
    parent: ViewGroup
) : BaseValidationResultVH<ValidationFaqVH.Item, CovidCertificateValidationResultFaqItemBinding>(
    R.layout.dcc_ticketing_validation_result_faq_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultFaqItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultFaqItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { _, _ ->

        faq.setTextWithUrl(
            R.string.dcc_ticketing_result_faq_text,
            R.string.dcc_ticketing_result_faq_label,
            R.string.dcc_ticketing_result_faq_link
        )
    }

    object Item : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
