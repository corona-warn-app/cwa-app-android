package de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common

import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BaseValidationResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.BusinessRuleVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.RuleHeaderVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.TechnicalValidationFailedVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationFaqVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationInputVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationOverallResultVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationPassedHintVH
import de.rki.coronawarnapp.covidcertificate.validation.ui.validationresult.common.listitem.ValidationResultItem
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod
import javax.inject.Inject

class ValidationResultAdapter @Inject constructor() :
    ModularAdapter<BaseValidationResultVH<ValidationResultItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<ValidationResultItem> {

    override val asyncDiffer: AsyncDiffer<ValidationResultItem> = AsyncDiffer(adapter = this)

    init {
        listOf(
            StableIdMod(data),
            DataBinderMod<ValidationResultItem, BaseValidationResultVH<ValidationResultItem, ViewBinding>>(data),
            TypedVHCreatorMod({ data[it] is BusinessRuleVH.Item }) { BusinessRuleVH(it) },
            TypedVHCreatorMod({ data[it] is RuleHeaderVH.Item }) { RuleHeaderVH(it) },
            TypedVHCreatorMod({ data[it] is TechnicalValidationFailedVH.Item }) { TechnicalValidationFailedVH(it) },
            TypedVHCreatorMod({ data[it] is ValidationFaqVH.Item }) { ValidationFaqVH(it) },
            TypedVHCreatorMod({ data[it] is ValidationInputVH.Item }) { ValidationInputVH(it) },
            TypedVHCreatorMod({ data[it] is ValidationOverallResultVH.Item }) { ValidationOverallResultVH(it) },
            TypedVHCreatorMod({ data[it] is ValidationPassedHintVH.Item }) { ValidationPassedHintVH(it) },
        ).run { modules.addAll(this) }
    }

    override fun getItemCount(): Int = data.size
}
