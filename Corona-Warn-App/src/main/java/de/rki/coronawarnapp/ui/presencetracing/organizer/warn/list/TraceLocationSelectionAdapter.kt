package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationSubHeaderItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationSubHeaderVH
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationVH
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class TraceLocationSelectionAdapter :
    ModularAdapter<TraceLocationSelectionAdapter.ItemVH<TraceLocationItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<TraceLocationItem> {

    override val asyncDiffer: AsyncDiffer<TraceLocationItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<TraceLocationItem, ItemVH<TraceLocationItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is TraceLocationVH.Item }) { TraceLocationVH(it) },
                TypedVHCreatorMod({ data[it] is TraceLocationSubHeaderItem }) { TraceLocationSubHeaderVH(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : TraceLocationItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
