package de.rki.coronawarnapp.tracing.ui.details.items.behavior

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemBehaviorIncreasedViewBinding
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.behavior.BehaviorIncreasedRiskBox.Item
import setTextWithUrl

class BehaviorIncreasedRiskBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout,
    private val openHomeInfo: () -> Unit,
    private val openHygieneInfo: () -> Unit
) : TracingDetailsAdapter.DetailsItemVH<Item, TracingDetailsItemBehaviorIncreasedViewBinding>(containerLayout, parent) {

    override val viewBinding = lazy {
        TracingDetailsItemBehaviorIncreasedViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        )
    }

    override val onBindData: TracingDetailsItemBehaviorIncreasedViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { _, _ ->
        lineHome.infoCallback { openHomeInfo() }
        lineHygiene.infoCallback { openHygieneInfo() }
        riskDetailsBehaviorBulletPoint1.setTextWithUrl(
            R.string.risk_details_behavior_increased_body_1,
            R.string.risk_details_behavior_increased_body_1_label,
            R.string.risk_details_behavior_increased_body_1_link
        )
        riskDetailsBehaviorBulletPoint2.setTextWithUrl(
            R.string.risk_details_behavior_increased_body_2,
            R.string.risk_details_behavior_increased_body_2_label,
            R.string.risk_details_behavior_increased_body_2_link
        )
    }

    object Item : BehaviorItem
}
