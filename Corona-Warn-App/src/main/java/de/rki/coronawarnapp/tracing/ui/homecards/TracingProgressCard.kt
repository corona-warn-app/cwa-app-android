package de.rki.coronawarnapp.tracing.ui.homecards

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentProgressViewBinding
import de.rki.coronawarnapp.tracing.states.RiskCalculationInProgress
import de.rki.coronawarnapp.tracing.ui.homecards.TracingProgressCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class TracingProgressCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_card_container_layout
) : HomeAdapter.HomeItemVH<Item, TracingContentProgressViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentProgressViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: TracingContentProgressViewBinding.(item: Item, payloads: List<Any>) -> Unit =
        { item, _ ->
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
            itemView.backgroundTintMode = PorterDuff.Mode.SRC_OVER
            itemView.backgroundTintList = ColorStateList.valueOf(item.state.getContainerColor(context))
            itemView.setOnClickListener { item.onCardClick(item) }
        }

    data class Item(
        val state: RiskCalculationInProgress,
        val onCardClick: (Item) -> Unit
    ) : TracingStateItem
}
