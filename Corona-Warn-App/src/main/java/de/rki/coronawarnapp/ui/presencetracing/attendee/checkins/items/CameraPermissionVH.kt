package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.TraceLocationAttendeeCheckinsItemCameraBinding

class CameraPermissionVH(parent: ViewGroup) :
    BaseCheckInVH<CameraPermissionVH.Item, TraceLocationAttendeeCheckinsItemCameraBinding>(
        layoutRes = R.layout.trace_location_attendee_checkins_item_camera,
        parent = parent
    ) {

    override val viewBinding: Lazy<TraceLocationAttendeeCheckinsItemCameraBinding> = lazy {
        TraceLocationAttendeeCheckinsItemCameraBinding.bind(itemView)
    }

    override val onBindData: TraceLocationAttendeeCheckinsItemCameraBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        openSettings.setOnClickListener {
            item.onOpenSettings()
        }
        itemView.setOnClickListener {
            item.onOpenSettings()
        }
    }

    data class Item(
        val onOpenSettings: () -> Unit
    ) : CheckInsItem {
        override val stableId: Long = Item::class.simpleName.hashCode().toLong()
    }
}
