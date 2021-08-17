package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationOrganizerTraceLocationsWarnSubheaderBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.TraceLocationsWarnAdapter

class TraceLocationSubheaderVH(parent: ViewGroup) :
    TraceLocationsWarnAdapter.ItemVH<OverviewSubHeaderItem, TraceLocationOrganizerTraceLocationsWarnSubheaderBinding>(
        layoutRes = R.layout.trace_location_organizer_trace_locations_warn_subheader,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationOrganizerTraceLocationsWarnSubheaderBinding> =
        lazy { TraceLocationOrganizerTraceLocationsWarnSubheaderBinding.bind(itemView) }

    override val onBindData: TraceLocationOrganizerTraceLocationsWarnSubheaderBinding.(
        item: OverviewSubHeaderItem,
        payloads: List<Any>
    ) -> Unit = { _, _ ->
        // NOOP
    }
}
