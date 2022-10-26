package de.rki.coronawarnapp.ui.main.home

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.familytest.ui.homecard.FamilyTestCard
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsHomeCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.PcrTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestErrorCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestInvalidCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestNegativeCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestOutdatedCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestPendingCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestPositiveCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestReadyCard
import de.rki.coronawarnapp.submission.ui.homecards.RapidTestSubmissionDoneCard
import de.rki.coronawarnapp.submission.ui.homecards.TestFetchingCard
import de.rki.coronawarnapp.submission.ui.homecards.TestUnregisteredCard
import de.rki.coronawarnapp.tracing.ui.homecards.IncreasedRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.LowRiskCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingDisabledCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingFailedCard
import de.rki.coronawarnapp.tracing.ui.homecards.TracingProgressCard
import de.rki.coronawarnapp.ui.main.home.items.CreateTraceLocationCard
import de.rki.coronawarnapp.ui.main.home.items.FAQCard
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.ui.main.home.items.IncompatibleCard
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.SavedStateMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class HomeAdapter :
    ModularAdapter<HomeAdapter.HomeItemVH<HomeItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<HomeItem> {

    override val asyncDiffer: AsyncDiffer<HomeItem> = AsyncDiffer(adapter = this)

    init {
        modules.addAll(
            listOf(
                StableIdMod(data),
                DataBinderMod<HomeItem, HomeItemVH<HomeItem, ViewBinding>>(data),
                TypedVHCreatorMod({ data[it] is FAQCard.Item }) { FAQCard(it) },
                TypedVHCreatorMod({ data[it] is IncompatibleCard.Item }) { IncompatibleCard(it) },
                TypedVHCreatorMod({ data[it] is CreateTraceLocationCard.Item }) { CreateTraceLocationCard(it) },
                TypedVHCreatorMod({ data[it] is FamilyTestCard.Item }) { FamilyTestCard(it) },
                TypedVHCreatorMod({ data[it] is IncreasedRiskCard.Item }) { IncreasedRiskCard(it) },
                TypedVHCreatorMod({ data[it] is LowRiskCard.Item }) { LowRiskCard(it) },
                TypedVHCreatorMod({ data[it] is TracingFailedCard.Item }) { TracingFailedCard(it) },
                TypedVHCreatorMod({ data[it] is TracingDisabledCard.Item }) { TracingDisabledCard(it) },
                TypedVHCreatorMod({ data[it] is TracingProgressCard.Item }) { TracingProgressCard(it) },
                TypedVHCreatorMod({ data[it] is TestFetchingCard.Item }) { TestFetchingCard(it) },
                TypedVHCreatorMod({ data[it] is PcrTestSubmissionDoneCard.Item }) { PcrTestSubmissionDoneCard(it) },
                TypedVHCreatorMod({ data[it] is PcrTestInvalidCard.Item }) { PcrTestInvalidCard(it) },
                TypedVHCreatorMod({ data[it] is PcrTestErrorCard.Item }) { PcrTestErrorCard(it) },
                TypedVHCreatorMod({ data[it] is PcrTestPositiveCard.Item }) { PcrTestPositiveCard(it) },
                TypedVHCreatorMod({ data[it] is PcrTestNegativeCard.Item }) { PcrTestNegativeCard(it) },
                TypedVHCreatorMod({ data[it] is PcrTestReadyCard.Item }) { PcrTestReadyCard(it) },
                TypedVHCreatorMod({ data[it] is PcrTestPendingCard.Item }) { PcrTestPendingCard(it) },
                TypedVHCreatorMod({ data[it] is RapidTestSubmissionDoneCard.Item }) { RapidTestSubmissionDoneCard(it) },
                TypedVHCreatorMod({ data[it] is RapidTestInvalidCard.Item }) { RapidTestInvalidCard(it) },
                TypedVHCreatorMod({ data[it] is RapidTestErrorCard.Item }) { RapidTestErrorCard(it) },
                TypedVHCreatorMod({ data[it] is RapidTestPositiveCard.Item }) { RapidTestPositiveCard(it) },
                TypedVHCreatorMod({ data[it] is RapidTestNegativeCard.Item }) { RapidTestNegativeCard(it) },
                TypedVHCreatorMod({ data[it] is RapidTestReadyCard.Item }) { RapidTestReadyCard(it) },
                TypedVHCreatorMod({ data[it] is RapidTestPendingCard.Item }) { RapidTestPendingCard(it) },
                TypedVHCreatorMod({ data[it] is RapidTestOutdatedCard.Item }) { RapidTestOutdatedCard(it) },
                TypedVHCreatorMod({ data[it] is TestUnregisteredCard.Item }) { TestUnregisteredCard(it) },
                TypedVHCreatorMod({ data[it] is StatisticsHomeCard.Item }) { StatisticsHomeCard(it) },
                SavedStateMod<HomeItemVH<HomeItem, ViewBinding>>() // For statistics card scroll position
            )
        )
    }

    override fun getItemCount(): Int = data.size

    abstract class HomeItemVH<Item : HomeItem, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : VH(layoutRes, parent), BindableVH<Item, VB>
}
