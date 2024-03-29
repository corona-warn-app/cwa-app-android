package de.rki.coronawarnapp.tracing.ui.details.items.risk

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentProgressViewBinding
import de.rki.coronawarnapp.tracing.states.RiskCalculationInProgress
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.risk.TracingProgressBox.Item

class TracingProgressBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_elevated_layout
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingContentProgressViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentProgressViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        ).also {
            it.root.elevation = resources.getDimension(R.dimen.elevation_10)
        }
    }

    override val onBindData: TracingContentProgressViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        item.state.apply {
            headline.text = getProgressCardHeadline(context)
            headline.setTextColor(getStableTextColor(context))
            itemView.setBackgroundColor(getContainerColor(context))
            detailsIcon.setColorFilter(getStableIconColor(context))
            detailsIcon.isGone = isInDetailsMode
            progressIndicator.setIndicatorColor(getStableIconColor(context))
            bodyText.text = getProgressCardBody(context)
            bodyText.setTextColor(getStableTextColor(context))
        }
    }

    data class Item(
        val state: RiskCalculationInProgress
    ) : RiskStateItem
}
