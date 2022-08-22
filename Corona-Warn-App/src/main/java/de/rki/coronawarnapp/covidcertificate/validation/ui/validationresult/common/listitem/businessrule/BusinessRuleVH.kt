package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.businessrule

import android.view.ViewGroup
import androidx.annotation.DrawableRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.DccValidationRule
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BaseValidationResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.EvaluatedField
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
import de.rki.coronawarnapp.util.lists.diffutil.update
import java.util.Locale

class BusinessRuleVH(
    parent: ViewGroup
) : BaseValidationResultVH<BusinessRuleVH.Item, CovidCertificateValidationResultRuleItemBinding>(
    R.layout.covid_certificate_validation_result_rule_item,
    parent
) {

    private val adapter: EvaluatedFieldAdapter by lazy { EvaluatedFieldAdapter() }

    override val viewBinding = lazy {
        CovidCertificateValidationResultRuleItemBinding.bind(itemView)
    }

    override val onBindData: CovidCertificateValidationResultRuleItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        with(curItem) {
            ruleIcon.setImageResource(ruleIconRes)

            ruleDescription.text = dccValidationRule.getRuleDescription()
            countryInformation.text = when (dccValidationRule.typeDcc) {
                DccValidationRule.Type.ACCEPTANCE -> context.getString(
                    R.string.validation_rules_acceptance_country,
                    countryName(dccValidationRule.country)
                )
                DccValidationRule.Type.INVALIDATION -> context.getString(R.string.validation_rules_invalidation_country)
                DccValidationRule.Type.BOOSTER_NOTIFICATION ->
                    throw IllegalStateException("Booster notification rules are not allowed here!")
            }
            adapter.update(affectedFields)
            ruleId.text = identifier

            if (evaluatedFieldList.adapter == null) {
                evaluatedFieldList.adapter = adapter
            }
        }
    }

    private fun countryName(countryCode: String?, userLocale: Locale = Locale.getDefault()): String =
        if (countryCode != null) {
            Locale(userLocale.language, countryCode.uppercase()).getDisplayCountry(userLocale)
        } else ""

    private fun DccValidationRule.getRuleDescription(): String {
        val currentLocaleCode = Locale.getDefault().language
        val descItem = description.find { it.languageCode == currentLocaleCode }
            ?: description.find { it.languageCode == "en" } ?: description.firstOrNull()
        return descItem?.description ?: identifier
    }

    data class Item(
        @DrawableRes val ruleIconRes: Int,
        val dccValidationRule: DccValidationRule,
        val affectedFields: List<EvaluatedField>,
        val identifier: String
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = identifier.hashCode().toLong()
    }
}
