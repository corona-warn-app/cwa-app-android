package de.rki.coronawarnapp.covidcertificate.person.ui.overview.items

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.PersonOverviewAdapter
import de.rki.coronawarnapp.databinding.CameraPermissionItemBinding
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer

class CameraPermissionCard(parent: ViewGroup) :
    PersonOverviewAdapter.PersonOverviewItemVH<CameraPermissionCard.Item, CameraPermissionItemBinding>(
        layoutRes = R.layout.camera_permission_item,
        parent = parent
    ) {

    override val viewBinding: Lazy<CameraPermissionItemBinding> = lazy { CameraPermissionItemBinding.bind(itemView) }

    override val onBindData: CameraPermissionItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().singleOrNull() ?: item
        openSettings.setOnClickListener { curItem.onOpenSettings() }
        itemView.setOnClickListener { curItem.onOpenSettings() }
    }

    data class Item(
        val onOpenSettings: () -> Unit
    ) : PersonCertificatesItem, HasPayloadDiffer {
        override val stableId: Long = Item::class.simpleName.hashCode().toLong()
        override fun diffPayload(old: Any, new: Any): Any? = if (old::class == new::class) new else null
    }
}
