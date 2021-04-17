package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeConsentHeaderBinding

class HeaderCheckInsVH(parent: ViewGroup) :
    CheckInsConsentAdapter.ItemVH<HeaderCheckInsVH.Item, TraceLocationAttendeeConsentHeaderBinding>(
        layoutRes = R.layout.trace_location_attendee_consent_header,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationAttendeeConsentHeaderBinding> = lazy {
        TraceLocationAttendeeConsentHeaderBinding.bind(itemView)
    }

    override val onBindData: TraceLocationAttendeeConsentHeaderBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
    }

    data class Item(
        val selectAll: () -> Unit
    ) : CheckInsConsentItem {
        override val stableId: Long = Item::class.simpleName.hashCode().toLong()
    }
}
