package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.validation.core.country.DccCountry
import de.rki.coronawarnapp.covidcertificate.validation.core.rule.EvaluatedDccRule
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleFailedItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class BusinessRuleFailedVH(
    parent: ViewGroup
) : BaseValidationResultVH<BusinessRuleFailedVH.Item, CovidCertificateValidationResultRuleFailedItemBinding>(
    R.layout.covid_certificate_validation_result_rule_container,
    parent
) {

    override val viewBinding = lazy {
        CovidCertificateValidationResultRuleFailedItemBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.container),
            true
        )
    }

    override val onBindData: CovidCertificateValidationResultRuleFailedItemBinding.(
        item: Item,
        payloads: List<Any>,
    ) -> Unit = { item, _ ->

        when (item.certificate) {
            is TestCertificate -> {
                if(
                    item.certificate.testType != CoronaTest.Type.PCR.raw &&
                    item.certificate.testType != CoronaTest.Type.RAPID_ANTIGEN.raw
                ) {

                    title.text = context.getString(R.string.validation_rules_failed_vh_title_wrong_test_type)
                    subtitle.text = context.getString(
                        R.string.validation_rules_open_vh_subtitle,
                        DccCountry(item.evaluatedDccRule.rule.country).displayName()
                    )
                    line1.text = context.getString(R.string.validation_rules_failed_vh_test_type)
                    line2.text = item.certificate.testType
                    line3.text = context.getString(R.string.validation_rules_failed_vh_rule_id)
                    line4.text = item.evaluatedDccRule.rule.identifier

                } else {
                    title.text = context.getString(R.string.validation_rules_failed_vh_title_uncertified_test_center)
                    subtitle.text = context.getString(R.string.validation_rules_failed_vh_subtitle_uncertified_test_center)
                    line1.text = context.getString(R.string.validation_rules_failed_vh_test_center_title)
                    line2.text = item.certificate.testCenter
                    line3.text = context.getString(R.string.validation_rules_failed_vh_rule_id)
                    line4.text = item.evaluatedDccRule.rule.identifier
                }
            }
        }
    }

    data class Item(
        val evaluatedDccRule: EvaluatedDccRule,
        val certificate: CwaCovidCertificate,
    ) : ValidationResultItem, HasPayloadDiffer {
        override val stableId: Long = evaluatedDccRule.rule.identifier.hashCode().toLong()

        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
