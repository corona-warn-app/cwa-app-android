package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.businessrule

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BaseValidationResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.EvaluatedField
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.ui.LazyString

class BusinessRuleVH(
    parent: ViewGroup
) : BaseValidationResultVH<BusinessRuleVH.Item, CovidCertificateValidationResultRuleItemBinding>(
    R.layout.covid_certificate_validation_result_rule_item,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultRuleItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultRuleItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        with(item) {
            ruleIcon.setImageResource(ruleIconRes)
            ruleDescription.text = ruleDescriptionText.get(context)
            countryInformation.text = countryInformationText.get(context)
            // TODO: Show affected fields
            ruleId.text = identifier
        }
    }

    data class Item(
        @DrawableRes val ruleIconRes: Int,
        val ruleDescriptionText: LazyString,
        val countryInformationText: LazyString,
        val affectedFields: List<EvaluatedField>,
        val identifier: String
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = identifier.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
