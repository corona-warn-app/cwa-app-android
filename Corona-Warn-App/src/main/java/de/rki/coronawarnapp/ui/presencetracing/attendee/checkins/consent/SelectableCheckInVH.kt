package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeConsentSelectableCheckInBinding
import de.rki.coronawarnapp.eventregistration.checkins.CheckIn
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class SelectableCheckInVH(parent: ViewGroup) :
    CheckInsConsentAdapter.ItemVH<SelectableCheckInVH.Item, TraceLocationAttendeeConsentSelectableCheckInBinding>(
        layoutRes = R.layout.trace_location_attendee_consent_selectable_check_in,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationAttendeeConsentSelectableCheckInBinding> = lazy {
        TraceLocationAttendeeConsentSelectableCheckInBinding.bind(itemView)
    }

    override val onBindData: TraceLocationAttendeeConsentSelectableCheckInBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
    }

    data class Item(
        val checkin: CheckIn,
        val onItemSelected: (CheckIn) -> Unit
    ) : CheckInsConsentItem, HasPayloadDiffer {
        override val stableId: Long = checkin.id
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
