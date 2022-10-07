package de.rki.coronawarnapp.ui.presencetracing.attendee.checkins

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.ActiveCheckInVH
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.CheckInsItem
import de.rki.coronawarnapp.ui.presencetracing.attendee.checkins.items.PastCheckInVH
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class CheckInsAdapter :
    ModularAdapter<CheckInsAdapter.ItemVH<CheckInsItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<CheckInsItem> {

    override val asyncDiffer: AsyncDiffer<CheckInsItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<CheckInsItem, ItemVH<CheckInsItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is ActiveCheckInVH.Item }) { ActiveCheckInVH(it) },
                TypedVHCreatorMod({ data[it] is PastCheckInVH.Item }) { PastCheckInVH(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : CheckInsItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
