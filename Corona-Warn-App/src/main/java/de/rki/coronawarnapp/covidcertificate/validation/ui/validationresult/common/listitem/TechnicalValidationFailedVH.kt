package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.DccValidation
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.TechnicalValidationFailedVH.Item
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultTechnicalFailedItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class TechnicalValidationFailedVH(
    parent: ViewGroup
) : BaseValidationResultVH<Item, CovidCertificateValidationResultTechnicalFailedItemBinding>(
    R.layout.covid_certificate_validation_result_rule_container,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultTechnicalFailedItemBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.container),
            true
        )
    }

    override val onBindData: CovidCertificateValidationResultTechnicalFailedItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->
        groupDateExpired.isGone = !item.dccValidation.expirationCheckPassed
        groupDateFormat.isGone = !item.dccValidation.jsonSchemaCheckPassed
        divider.isGone = !item.dccValidation.expirationCheckPassed || !item.dccValidation.jsonSchemaCheckPassed
    }

    data class Item(
        val dccValidation: DccValidation,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
