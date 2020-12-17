package de.rki.coronawarnapp.tracing.ui.details.items.periodlogged

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemPeriodloggedViewBinding
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem

class PeriodLoggedBox(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.tracing_details_item_container_layout
) : TracingDetailsAdapter.DetailsItemVH<PeriodLoggedBox.Item, TracingDetailsItemPeriodloggedViewBinding>(
    containerLayout,
    parent
) {

    override val viewBinding = lazy {
        TracingDetailsItemPeriodloggedViewBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.box_container),
            true
        )
    }

    override val onBindData: TracingDetailsItemPeriodloggedViewBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        loggedPeriod = item
    }

    data class Item(
        val activeTracingDaysInRetentionPeriod: Int
    ) : DetailsItem {

        fun getRiskActiveTracingDaysInRetentionPeriodLogged(c: Context): String = c.getString(
            R.string.risk_details_information_body_period_logged_assessment
        ).format(activeTracingDaysInRetentionPeriod)

        override val stableId: Long
            get() = Item::class.java.name.hashCode().toLong()
    }
}
