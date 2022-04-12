package de.rki.coronawarnapp.ui.coronatest.rat.profile.list.items

import android.view.ViewGroup
import androidx.core.view.isVisible
import coil.loadAny
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.ProfileCardItemBinding
import de.rki.coronawarnapp.ui.coronatest.rat.profile.list.ProfilesListAdapter
import de.rki.coronawarnapp.ui.coronatest.rat.profile.qrcode.PersonProfile
import de.rki.coronawarnapp.util.coil.loadingView
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode

class ProfileCard(parent: ViewGroup) : ProfilesListAdapter.ItemVH<ProfileCard.Item, ProfileCardItemBinding>(
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
            if (curItem.personProfile.profile != null) {
                append(curItem.personProfile.profile.firstName)
                append(" ${curItem.personProfile.profile.lastName}")
            }
        }
        name.isVisible = fullName.isNotBlank()
        name.text = fullName
        if (curItem.personProfile.qrCode != null) {
            val request = curItem.personProfile.qrCode.let { CoilQrCode(content = it) }
            qrCodeCard.loadAny(request) {
                crossfade(true)
                loadingView(qrCodeCard, progressBar)
            }
        }
        itemView.apply {
            setOnClickListener { curItem.onClickAction(curItem, bindingAdapterPosition) }
            transitionName = curItem.personProfile.toString()
        }
    }

    data class Item(
        val personProfile: PersonProfile,
        val onClickAction: (Item, Int) -> Unit
    ) : ProfileListItem {
        override val stableId: Long = personProfile.profile.hashCode().toLong()
    }
}
