package de.rki.coronawarnapp.tracing.ui.details.items.periodlogged

import android.content.Context
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TracingDetailsItemPeriodloggedViewBinding
import de.rki.coronawarnapp.tracing.GeneralTracingStatus
import de.rki.coronawarnapp.tracing.ui.details.TracingDetailsAdapter
import de.rki.coronawarnapp.tracing.ui.details.items.DetailsItem
import de.rki.coronawarnapp.util.ContextExtensions.getColorCompat

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
        val activeTracingDaysInRetentionPeriod: Int,
        val tracingStatus: GeneralTracingStatus.Status
    ) : DetailsItem {

        fun getRiskActiveTracingDaysInRetentionPeriodLogged(context: Context): String = context.getString(
            R.string.risk_details_information_body_period_logged_assessment
        ).format(activeTracingDaysInRetentionPeriod)

        fun getProgressColor(context: Context) = when (tracingStatus) {
            GeneralTracingStatus.Status.TRACING_INACTIVE,
            GeneralTracingStatus.Status.BLUETOOTH_DISABLED,
            GeneralTracingStatus.Status.LOCATION_DISABLED -> R.color.colorTextPrimary2
            GeneralTracingStatus.Status.TRACING_ACTIVE -> R.color.colorAccentTintIcon
        }.let { context.getColorCompat(it) }

        override val stableId: Long
            get() = Item::class.java.name.hashCode().toLong()
    }
}
