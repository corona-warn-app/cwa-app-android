package de.rki.coronawarnapp.ui.main.home

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.main.home.items.faq.FAQCardVH
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestErrorCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestFetchingCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestInvalidCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestNegativeCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestPendingCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestPositiveCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestReadyCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestSubmissionDoneCard
import de.rki.coronawarnapp.ui.main.home.items.testresult.TestUnregisteredCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.IncreasedRiskCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.LowRiskCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.TracingDisabledCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.TracingFailedCard
import de.rki.coronawarnapp.ui.main.home.items.tracing.TracingProgressCard
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class HomeAdapter : ModularAdapter<HomeAdapter.HomeItemVH<HomeItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<HomeItem> {

    override val asyncDiffer: AsyncDiffer<HomeItem> = AsyncDiffer(this)

    init {
        modules.apply {
            add(StableIdMod(data))
            add(DataBinderMod<HomeItem, HomeItemVH<HomeItem, ViewBinding>>(data))
            add(TypedVHCreatorMod({ data[it] is FAQCardVH.Item }) { FAQCardVH(it) })
            add(TypedVHCreatorMod({ data[it] is IncreasedRiskCard.Item }) { IncreasedRiskCard(it) })
            add(TypedVHCreatorMod({ data[it] is LowRiskCard.Item }) { LowRiskCard(it) })
            add(TypedVHCreatorMod({ data[it] is TracingFailedCard.Item }) { TracingFailedCard(it) })
            add(TypedVHCreatorMod({ data[it] is TracingDisabledCard.Item }) { TracingDisabledCard(it) })
            add(TypedVHCreatorMod({ data[it] is TracingProgressCard.Item }) { TracingProgressCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestSubmissionDoneCard.Item }) { TestSubmissionDoneCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestInvalidCard.Item }) { TestInvalidCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestErrorCard.Item }) { TestErrorCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestFetchingCard.Item }) { TestFetchingCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestPositiveCard.Item }) { TestPositiveCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestNegativeCard.Item }) { TestNegativeCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestReadyCard.Item }) { TestReadyCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestPendingCard.Item }) { TestPendingCard(it) })
            add(TypedVHCreatorMod({ data[it] is TestUnregisteredCard.Item }) { TestUnregisteredCard(it) })
        }
    }

    override fun getItemCount(): Int = data.size

    abstract class HomeItemVH<Item : HomeItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int, parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
