package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayOverviewItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.day.DayOverviewVH
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader.OverviewSubHeaderItem
import de.rki.coronawarnapp.contactdiary.ui.overview.adapter.subheader.OverviewSubHeaderVH
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class DiaryOverviewAdapter :
    ModularAdapter<DiaryOverviewAdapter.ItemVH<DiaryOverviewItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<DiaryOverviewItem> {

    override val asyncDiffer: AsyncDiffer<DiaryOverviewItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<DiaryOverviewItem, ItemVH<DiaryOverviewItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is DayOverviewItem }) { DayOverviewVH(it) },
                TypedVHCreatorMod({ data[it] is OverviewSubHeaderItem }) { OverviewSubHeaderVH(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : DiaryOverviewItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
