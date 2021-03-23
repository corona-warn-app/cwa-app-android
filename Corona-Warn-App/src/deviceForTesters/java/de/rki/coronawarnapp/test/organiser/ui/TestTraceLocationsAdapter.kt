package de.rki.coronawarnapp.test.organiser.ui

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.test.organiser.ui.items.TestTraceLocationVH
import de.rki.coronawarnapp.test.organiser.ui.items.TestTraceLocationItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class TestTraceLocationsAdapter :
    ModularAdapter<TestTraceLocationsAdapter.ItemVH<TestTraceLocationItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<TestTraceLocationItem> {

    override val asyncDiffer: AsyncDiffer<TestTraceLocationItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<TestTraceLocationItem, ItemVH<TestTraceLocationItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is TestTraceLocationVH.Item }) { TestTraceLocationVH(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item : TestTraceLocationItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
