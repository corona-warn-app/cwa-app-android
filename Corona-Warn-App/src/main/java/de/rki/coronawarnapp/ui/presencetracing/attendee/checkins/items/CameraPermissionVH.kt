package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.CameraPermissionItemBinding

class CameraPermissionVH(parent: ViewGroup) :
    BaseCheckInVH<CameraPermissionVH.Item, CameraPermissionItemBinding>(
        layoutRes = R.layout.camera_permission_item,
        parent = parent
    ) {

    override val viewBinding: Lazy<CameraPermissionItemBinding> = lazy {
        CameraPermissionItemBinding.bind(itemView)
    }

    override val onBindData: CameraPermissionItemBinding.(
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
