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
import de.rki.coronawarnapp.util.ui.toResolvingString
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
            val (countryText, countryCode) = countryInformationText
            ruleIcon.setImageResource(ruleIconRes)

            ruleDescription.text = ruleDescriptionText.getRuleDescription()
            countryInformation.text = countryText.toResolvingString(countryName(countryCode)).get(context)
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

    fun DccValidationRule.getRuleDescription(): String {
        val currentLocaleCode = Locale.getDefault().language
        val descItem = description.find { it.languageCode == currentLocaleCode }
            ?: description.find { it.languageCode == "en" } ?: description.firstOrNull()
        return descItem?.description ?: identifier
    }

    data class Item(
        @DrawableRes val ruleIconRes: Int,
        val ruleDescriptionText: DccValidationRule,
        val countryInformationText: Pair<Int, String?>,
        val affectedFields: List<EvaluatedField>,
        val identifier: String
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = identifier.hashCode().toLong()
    }
}
