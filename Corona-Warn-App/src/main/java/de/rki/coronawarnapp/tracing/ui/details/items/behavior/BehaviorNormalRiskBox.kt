package de.rki.coronawarnapp.tracing.ui.details.items.behavior

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemBehaviorNormalViewBinding
import de.rki.coronawarnapp.risk.RiskState
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorNormalRiskBox.Item
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

class BehaviorNormalRiskBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingDetailsItemBehaviorNormalViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingDetailsItemBehaviorNormalViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        )
    }

    override val onBindData: TracingDetailsItemBehaviorNormalViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        state = item
    }

    data class Item(
        val tracingStatus: GeneralTracingStatus.Status,
        val riskState: RiskState
    ) : BehaviorItem {
        fun getIconColor(context: Context) = when {
            tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> R.color.colorTextSemanticNeutral
            riskState == RiskState.INCREASED_RISK || riskState == RiskState.LOW_RISK -> R.color.colorStableLight
            else -> R.color.colorTextSemanticNeutral
        }.let { context.getColorCompat(it) }

        fun getBackgroundColor(context: Context) = when {
            tracingStatus == GeneralTracingStatus.Status.TRACING_INACTIVE -> R.color.colorSurface2
            riskState == RiskState.INCREASED_RISK -> R.color.colorSemanticHighRisk
            riskState == RiskState.LOW_RISK -> R.color.colorSemanticLowRisk
            else -> R.color.colorSurface2
        }.let { context.getColorCompat(it) }
    }
}
