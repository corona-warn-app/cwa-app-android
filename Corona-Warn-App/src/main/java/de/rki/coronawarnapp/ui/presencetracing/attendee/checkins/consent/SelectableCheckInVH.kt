package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.consent

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeConsentSelectableCheckInBinding
import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.common.checkoutInfo
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
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        val checkIn = curItem.checkIn
        val imageResource = if (checkIn.hasSubmissionConsent) R.drawable.ic_selected else R.drawable.ic_unselected

        checkbox.setImageResource(imageResource)
        title.text = checkIn.description
        subtitle.text = checkIn.address
        checkoutInfo.text = checkIn.checkoutInfo

        checkbox.setOnClickListener { item.onItemSelected(checkIn) }
        itemView.setOnClickListener { item.onItemSelected(checkIn) }
    }

    data class Item(
        val checkIn: CheckIn,
        val onItemSelected: (CheckIn) -> Unit
    ) : CheckInsConsentItem, HasPayloadDiffer {
        override val stableId: Long = checkIn.id
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
