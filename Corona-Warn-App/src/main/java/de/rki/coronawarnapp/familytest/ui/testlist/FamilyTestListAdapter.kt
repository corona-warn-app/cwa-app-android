package de.rki.coronawarnapp.familytest.ui.testlist

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestOutdatedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyTestListItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class FamilyTestListAdapter :
    ModularAdapter<FamilyTestListAdapter.FamilyTestListVH<FamilyTestListItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<FamilyTestListItem> {

    override val asyncDiffer: AsyncDiffer<FamilyTestListItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<FamilyTestListItem, FamilyTestListVH<FamilyTestListItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is FamilyTestListCard.Item }) { FamilyTestListCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyPcrTestRedeemedCard.Item }) { FamilyPcrTestRedeemedCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyRapidTestOutdatedCard.Item }) { FamilyRapidTestOutdatedCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyRapidTestRedeemedCard.Item }) { FamilyRapidTestRedeemedCard(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class FamilyTestListVH<Item : FamilyTestListItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
