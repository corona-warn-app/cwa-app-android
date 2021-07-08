package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultHeaderItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

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
        headline.setText(
            when (item.state) {
                DccValidation.State.PASSED -> R.string.validation_rules_result_valid_result_title
                DccValidation.State.OPEN -> R.string.validation_rules_result_cannot_be_checked_result_title
                DccValidation.State.TECHNICAL_FAILURE -> R.string.validation_rules_result_not_valid_result_title
                DccValidation.State.FAILURE -> R.string.validation_rules_result_not_valid_result_title
            }
        )
    }

    data class Item(
        val state: DccValidation.State,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
