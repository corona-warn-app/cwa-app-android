package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultHeaderItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.LazyString

class ValidationOverallResultVH(
    parent: ViewGroup
) : BaseValidationResultVH<ValidationOverallResultVH.Item, CovidCertificateValidationResultHeaderItemBinding>(
    R.layout.covid_certificate_validation_result_header_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultHeaderItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultHeaderItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        headline.text = curItem.headlineText.get(context)
    }

    data class Item(
        val headlineText: LazyString
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
