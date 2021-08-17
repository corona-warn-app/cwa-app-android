package de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader.OverviewSubHeaderVH
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.OverviewSubHeaderItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationSubheaderVH
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationWarnItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.warn.list.items.TraceLocationVH
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class TraceLocationsWarnAdapter :
    ModularAdapter<TraceLocationsWarnAdapter.ItemVH<TraceLocationWarnItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<TraceLocationWarnItem> {

    override val asyncDiffer: AsyncDiffer<TraceLocationWarnItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<TraceLocationWarnItem, ItemVH<TraceLocationWarnItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is TraceLocationVH.Item }) { TraceLocationVH(it) },
                TypedVHCreatorMod({ data[it] is OverviewSubHeaderItem }) { TraceLocationSubheaderVH(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : TraceLocationWarnItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
