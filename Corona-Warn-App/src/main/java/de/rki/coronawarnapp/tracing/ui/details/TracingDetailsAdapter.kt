package de.rki.coronawarnapp.tracing.ui.details

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem
import de.rki.coronawarnapp.tracing.ui.details.items.additionalinfos.AdditionalInfoLowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorNormalRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.periodlogged.PeriodLoggedBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.IncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.LowRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingDisabledBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingFailedBox
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingProgressBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsFailedCalculationBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsIncreasedRiskBox
import de.rki.coronawarnapp.tracing.ui.details.items.riskdetails.DetailsLowRiskBox
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class TracingDetailsAdapter : ModularAdapter<TracingDetailsAdapter.DetailsItemVH<DetailsItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<DetailsItem> {

    override val asyncDiffer: AsyncDiffer<DetailsItem> = AsyncDiffer(this)

    init {
        modules.addAll(listOf(
            StableIdMod(data),
            DataBinderMod<DetailsItem, DetailsItemVH<DetailsItem, ViewBinding>>(data),
            TypedVHCreatorMod({ data[it] is IncreasedRiskBox.Item }) { IncreasedRiskBox(it) },
            TypedVHCreatorMod({ data[it] is LowRiskBox.Item }) { LowRiskBox(it) },
            TypedVHCreatorMod({ data[it] is TracingFailedBox.Item }) { TracingFailedBox(it) },
            TypedVHCreatorMod({ data[it] is TracingDisabledBox.Item }) { TracingDisabledBox(it) },
            TypedVHCreatorMod({ data[it] is TracingProgressBox.Item }) { TracingProgressBox(it) },
            TypedVHCreatorMod({ data[it] is DetailsFailedCalculationBox.Item }) { DetailsFailedCalculationBox(it) },
            TypedVHCreatorMod({ data[it] is DetailsIncreasedRiskBox.Item }) { DetailsIncreasedRiskBox(it) },
            TypedVHCreatorMod({ data[it] is DetailsLowRiskBox.Item }) { DetailsLowRiskBox(it) },
            TypedVHCreatorMod({ data[it] is PeriodLoggedBox.Item }) { PeriodLoggedBox(it) },
            TypedVHCreatorMod({ data[it] is BehaviorIncreasedRiskBox.Item }) { BehaviorIncreasedRiskBox(it) },
            TypedVHCreatorMod({ data[it] is BehaviorNormalRiskBox.Item }) { BehaviorNormalRiskBox(it) },
            TypedVHCreatorMod({ data[it] is AdditionalInfoLowRiskBox.Item }) { AdditionalInfoLowRiskBox(it) }
        ))
    }

    override fun getItemCount(): Int = data.size

    abstract class DetailsItemVH<Item : DetailsItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
