package de.rki.coronawarnapp.tracing.ui.homecards

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isGone
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingContentLowViewBinding
import de.rki.coronawarnapp.tracing.states.LowRisk
import de.rki.coronawarnapp.tracing.ui.homecards.LowRiskCard.Item
import de.rki.coronawarnapp.ui.main.home.HomeAdapter

class LowRiskCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_card_container_layout
) : HomeAdapter.HomeItemVH<Item, TracingContentLowViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingContentLowViewBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: TracingContentLowViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        item.state.apply {
            detailsIcon.isGone = isInDetailsMode
            rowContact.contentDescription = getRiskContactBodyDescription(context)
            rowContact.setText(getRiskContactBody(context))
            rowContactLast.isGone = getRiskContactLast(context) == null
            rowContactLast.setText(getRiskContactLast(context) ?: "")
            rowDaysSinceInstallation.isGone = appInstalledForOverTwoWeeks()
            rowDaysSinceInstallation.setText(getDaysSinceInstall(context))
            rowTimeFetched.setText(getTimeFetched(context))
            updateAction.isGone = !showUpdateButton
        }
        itemView.setOnClickListener { item.onCardClick(item) }
        updateAction.setOnClickListener { item.onUpdateClick(item) }
    }

    data class Item(
        val state: LowRisk,
        val onCardClick: (Item) -> Unit,
        val onUpdateClick: (Item) -> Unit
    ) : TracingStateItem
}
