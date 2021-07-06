package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultPassedHintItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class ValidationPassedHintVH(
    parent: ViewGroup
) : BaseValidationResultVH<ValidationPassedHintVH.Item, CovidCertificateValidationResultPassedHintItemBinding>(
    R.layout.covid_certificate_validation_result_passed_hint_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultPassedHintItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultPassedHintItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        // TODO
        // Both technical failure reasons, using `isGone`
    }

    object Item : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
