package de.rki.coronawarnapp.dccticketing.ui.validationresult

import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.BaseValidationResultVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ResultRuleVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.DescriptionVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.ValidationFaqVH
import de.rki.coronawarnapp.dccticketing.ui.validationresult.items.TestingInfoVH
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
            TypedVHCreatorMod({ data[it] is DescriptionVH.Item }) { DescriptionVH(it) },
            TypedVHCreatorMod({ data[it] is TestingInfoVH.Item }) { TestingInfoVH(it) },
            TypedVHCreatorMod({ data[it] is ResultRuleVH.Item }) { ResultRuleVH(it) },
        ).run { modules.addAll(this) }
    }

    override fun getItemCount(): Int = data.size
}
