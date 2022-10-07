package de.rki.coronawarnapp.profile.ui.list

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.profile.ui.list.items.ProfileCard
import de.rki.coronawarnapp.profile.ui.list.items.ProfileListItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class ProfileListAdapter :
    ModularAdapter<ProfileListAdapter.ItemVH<ProfileListItem, ViewBinding>>(), AsyncDiffUtilAdapter<ProfileListItem> {

    override val asyncDiffer: AsyncDiffer<ProfileListItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<ProfileListItem, ItemVH<ProfileListItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is ProfileCard.Item }) { ProfileCard(it) }
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : ProfileListItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
