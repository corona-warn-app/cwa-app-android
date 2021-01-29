package de.rki.coronawarnapp.ui.main.home

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsHomeCard
import de.rki.coronawarnapp.submission.ui.homecards.TestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.TestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.TestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.TestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.TestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.TestUnregisteredCard
import de.rki.coronawarnapp.tracing.ui.homecards.IncreasedRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.LowRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingDisabledCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingFailedCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingProgressCard
import de.rki.coronawarnapp.ui.main.home.items.DiaryCard
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class HomeAdapter : ModularAdapter<HomeAdapter.HomeItemVH<HomeItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<HomeItem> {

    override val asyncDiffer: AsyncDiffer<HomeItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(listOf(
            StableIdMod(data),
            DataBinderMod<HomeItem, HomeItemVH<HomeItem, ViewBinding>>(data),
            TypedVHCreatorMod({ data[it] is FAQCard.Item }) { FAQCard(it) },
            TypedVHCreatorMod({ data[it] is IncreasedRiskCard.Item }) { IncreasedRiskCard(it) },
            TypedVHCreatorMod({ data[it] is LowRiskCard.Item }) { LowRiskCard(it) },
            TypedVHCreatorMod({ data[it] is TracingFailedCard.Item }) { TracingFailedCard(it) },
            TypedVHCreatorMod({ data[it] is TracingDisabledCard.Item }) { TracingDisabledCard(it) },
            TypedVHCreatorMod({ data[it] is TracingProgressCard.Item }) { TracingProgressCard(it) },
            TypedVHCreatorMod({ data[it] is TestSubmissionDoneCard.Item }) { TestSubmissionDoneCard(it) },
            TypedVHCreatorMod({ data[it] is TestInvalidCard.Item }) { TestInvalidCard(it) },
            TypedVHCreatorMod({ data[it] is TestErrorCard.Item }) { TestErrorCard(it) },
            TypedVHCreatorMod({ data[it] is TestFetchingCard.Item }) { TestFetchingCard(it) },
            TypedVHCreatorMod({ data[it] is TestPositiveCard.Item }) { TestPositiveCard(it) },
            TypedVHCreatorMod({ data[it] is TestNegativeCard.Item }) { TestNegativeCard(it) },
            TypedVHCreatorMod({ data[it] is TestReadyCard.Item }) { TestReadyCard(it) },
            TypedVHCreatorMod({ data[it] is TestPendingCard.Item }) { TestPendingCard(it) },
            TypedVHCreatorMod({ data[it] is TestUnregisteredCard.Item }) { TestUnregisteredCard(it) },
            TypedVHCreatorMod({ data[it] is DiaryCard.Item }) { DiaryCard(it) },
            TypedVHCreatorMod({ data[it] is StatisticsHomeCard.Item }) { StatisticsHomeCard(it) }
        ))
    }

    override fun getItemCount(): Int = data.size

    abstract class HomeItemVH<Item : HomeItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
