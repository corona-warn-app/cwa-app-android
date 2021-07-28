package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import TextViewUrlSet
import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultFaqItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.toResolvingString
import setTextWithUrls

class ValidationFaqVH(
    parent: ViewGroup
) : BaseValidationResultVH<ValidationFaqVH.Item, CovidCertificateValidationResultFaqItemBinding>(
    R.layout.covid_certificate_validation_result_faq_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultFaqItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultFaqItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { _, _ ->

        faq.setTextWithUrls(
            R.string.validation_start_faq.toResolvingString(),
            TextViewUrlSet(
                labelResource = R.string.validation_start_faq_label,
                urlResource = R.string.validation_start_faq_link
            ),
            TextViewUrlSet(
                labelResource = R.string.validation_start_reopen_europe_label,
                urlResource = R.string.validation_start_reopen_europe_link
            )
        )
    }

    object Item : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
