package de.rki.coronawarnapp.tracing.ui.details.items.risk

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentFailedViewBinding
import de.rki.coronawarnapp.tracing.states.RiskCalculationFailed
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingFailedBox.Item

class TracingFailedBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_elevated_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingContentFailedViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentFailedViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        ).also {
            it.root.elevation = resources.getDimension(R.dimen.elevation_10)
        }
    }

    override val onBindData: TracingContentFailedViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        item.state.apply {
            riskCardHeaderIcon.isGone = isInDetailsMode
            riskCardButtonUpdate.isGone = !showRestartButton
            riskCardButtonUpdate.isGone = isInDetailsMode
            riskCardHeaderIcon.isGone = isInDetailsMode
        }
    }

    data class Item(
        val state: RiskCalculationFailed
    ) : RiskStateItem
}
