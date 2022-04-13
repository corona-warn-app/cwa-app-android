package de.rki.coronawarnapp.ui.coronatest.rat.profile.list.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import coil.loadAny
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileCardItemBinding
import de.rki.coronawarnapp.profile.model.Profile
import de.rki.coronawarnapp.ui.coronatest.rat.profile.list.ProfileListAdapter
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

class ProfileCard(parent: ViewGroup) : ProfileListAdapter.ItemVH<ProfileCard.Item, ProfileCardItemBinding>(
    layoutRes = R.layout.home_card_container_layout,
    parent
) {

    override val viewBinding = lazy {
        ProfileCardItemBinding.inflate(layoutInflater, itemView.findViewById(R.id.card_container), true)
    }

    override val onBindData: ProfileCardItemBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item
        val fullName = buildString {
            if (curItem.profile.firstName.isNotBlank()) {
                append(curItem.profile.firstName.trim())
            }
            if (curItem.profile.lastName.isNotBlank()) {
                append(" ${curItem.profile.lastName.trim()}")
            }
        }

        name.isVisible = fullName.isNotBlank()
        name.text = fullName
        val request = curItem.qrCode.let { CoilQrCode(content = it) }
        qrCodeCard.loadAny(request) {
            crossfade(true)
            loadingView(qrCodeCard, progressBar)
        }
        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, bindingAdapterPosition) }
            transitionName = curItem.toString()
        }
    }

    data class Item(
        val profile: Profile,
        val qrCode: String,
        val onClickAction: (Item, Int) -> Unit
    ) : ProfileListItem {
        override val stableId: Long = profile.id.hashCode().toLong()
    }
}
