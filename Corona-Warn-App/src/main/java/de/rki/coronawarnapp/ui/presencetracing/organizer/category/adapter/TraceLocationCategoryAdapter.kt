package de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategory
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.category.TraceLocationCategoryVH
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.header.TraceLocationHeaderItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.header.TraceLocationHeaderVH
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.separator.TraceLocationSeparatorItem
import de.rki.coronawarnapp.ui.presencetracing.organizer.category.adapter.separator.TraceLocationSeparatorVH
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class TraceLocationCategoryAdapter(
    categoryList: List<CategoryItem>,
    private val itemClickListener: (category: TraceLocationCategory) -> Unit
) : ModularAdapter<TraceLocationCategoryAdapter.ItemVH<CategoryItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<CategoryItem> {

    override val asyncDiffer: AsyncDiffer<CategoryItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<CategoryItem, ItemVH<CategoryItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is TraceLocationHeaderItem }) { TraceLocationHeaderVH(it) },
                TypedVHCreatorMod({ data[it] is TraceLocationCategory }) {
                    TraceLocationCategoryVH(
                        it,
                        itemClickListener
                    )
                },
                TypedVHCreatorMod({ data[it] is TraceLocationSeparatorItem }) { TraceLocationSeparatorVH(it) }
            )
        )
        update(categoryList)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    abstract class ItemVH<Item : CategoryItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
