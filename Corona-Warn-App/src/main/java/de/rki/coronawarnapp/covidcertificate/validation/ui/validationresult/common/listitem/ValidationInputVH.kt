package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultInputItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.LazyString

class ValidationInputVH(
    parent: ViewGroup
) : BaseValidationResultVH<ValidationInputVH.Item, CovidCertificateValidationResultInputItemBinding>(
    R.layout.covid_certificate_validation_result_input_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultInputItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultInputItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        dateDetailsTv.text = curItem.dateDetails.get(context)
    }

    data class Item(
        val dateDetails: LazyString
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()
    }
}
