package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultFaqItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

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
    ) -> Unit = { item, payloads ->
        // TODO
    }

    object Item : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
