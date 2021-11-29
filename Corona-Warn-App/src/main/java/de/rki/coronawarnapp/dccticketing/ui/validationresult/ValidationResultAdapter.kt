package de.rki.coronawarnapp.dccticketing.ui.validationresult

import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.BaseValidationResultVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.BusinessRuleVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.RuleHeaderVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationFaqVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationInputVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationResultItem
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
            TypedVHCreatorMod({ data[it] is ValidationFaqVH.Item }) { ValidationFaqVH(it) },
            TypedVHCreatorMod({ data[it] is RuleHeaderVH.Item }) { RuleHeaderVH(it) },
            TypedVHCreatorMod({ data[it] is ValidationInputVH.Item }) { ValidationInputVH(it) },
            TypedVHCreatorMod({ data[it] is BusinessRuleVH.Item }) { BusinessRuleVH(it) },
            // TODO: more items here
        ).run { modules.addAll(this) }
    }

    override fun getItemCount(): Int = data.size
}
