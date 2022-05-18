package de.rki.coronawarnapp.familytest.ui.testlist

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyPcrTestRedeemedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestInvalidCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestNegativeCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestOutdatedCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestPendingCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestPositiveCard
import de.rki.coronawarnapp.familytest.ui.testlist.items.FamilyRapidTestRedeemedCard
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
                // PCR
                TypedVHCreatorMod({ data[it] is FamilyPcrTestCard.Item }) { FamilyPcrTestCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyPcrTestRedeemedCard.Item }) { FamilyPcrTestRedeemedCard(it) },
                // Rapid
                TypedVHCreatorMod({ data[it] is FamilyRapidTestPendingCard.Item }) { FamilyRapidTestPendingCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyRapidTestNegativeCard.Item }) { FamilyRapidTestNegativeCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyRapidTestPositiveCard.Item }) { FamilyRapidTestPositiveCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyRapidTestInvalidCard.Item }) { FamilyRapidTestInvalidCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyRapidTestOutdatedCard.Item }) { FamilyRapidTestOutdatedCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyRapidTestRedeemedCard.Item }) { FamilyRapidTestRedeemedCard(it) },
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class FamilyTestListVH<Item : FamilyTestListItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
