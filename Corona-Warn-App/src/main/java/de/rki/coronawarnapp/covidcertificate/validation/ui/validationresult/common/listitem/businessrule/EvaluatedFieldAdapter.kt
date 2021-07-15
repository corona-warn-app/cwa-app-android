package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.businessrule

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.EvaluatedField
import de.rki.coronawarnapp.databinding.CovidCertificateValidationResultRuleItemEvaluatedFieldItemBinding
import de.rki.coronawarnapp.ui.lists.BaseAdapter
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer

class EvaluatedFieldAdapter :
    BaseAdapter<EvaluatedFieldAdapter.VH>(),
    AsyncDiffUtilAdapter<EvaluatedField> {

    override val asyncDiffer: AsyncDiffer<EvaluatedField> = AsyncDiffer(this)

    override fun onCreateBaseVH(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindBaseVH(holder: VH, position: Int, payloads: MutableList<Any>) =
        holder.bind(data[position], payloads)

    override fun getItemCount(): Int = data.size

    inner class VH(parent: ViewGroup) :
        BaseAdapter.VH(R.layout.covid_certificate_validation_result_rule_item_evaluated_field_item, parent),
        BindableVH<EvaluatedField, CovidCertificateValidationResultRuleItemEvaluatedFieldItemBinding> {

        override val viewBinding: Lazy<CovidCertificateValidationResultRuleItemEvaluatedFieldItemBinding> =
            lazy { CovidCertificateValidationResultRuleItemEvaluatedFieldItemBinding.bind(itemView) }

        override val onBindData: CovidCertificateValidationResultRuleItemEvaluatedFieldItemBinding
        .(item: EvaluatedField, payloads: List<Any>) -> Unit =
            { item, _ ->
                with(item) {
                    title.text = context.getString(fieldResourceId)
                    subtitle.text = certificateFieldValue
                }
            }
    }
}
