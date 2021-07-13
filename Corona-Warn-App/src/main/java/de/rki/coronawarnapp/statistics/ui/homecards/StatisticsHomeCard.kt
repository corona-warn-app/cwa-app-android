package de.rki.coronawarnapp.statistics.ui.homecards

import android.os.Parcelable
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsScrollcontainerBinding
import de.rki.coronawarnapp.statistics.AddStatsItem
import de.rki.coronawarnapp.statistics.GenericStatsItem
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.LocalStatsItem
import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.ui.homecards.cards.AddLocalStatisticsCardItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.GlobalStatisticsCardItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.LocalStatisticsCardItem
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.isPhone
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.lists.modular.mods.SavedStateMod

class StatisticsHomeCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_statistics_scrollcontainer
) : HomeAdapter.HomeItemVH<StatisticsHomeCard.Item, HomeStatisticsScrollcontainerBinding>(containerLayout, parent),
    SavedStateMod.StateSavingVH {

    override var savedStateKey: String? = null

    private val statisticsLayoutManager: StatisticsLayoutManager by lazy {
        StatisticsLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private val statisticsCardAdapter by lazy { StatisticsCardAdapter() }

    override val viewBinding = lazy {
        HomeStatisticsScrollcontainerBinding.bind(itemView).apply {
            statisticsRecyclerview.apply {
                setHasFixedSize(false)
                adapter = statisticsCardAdapter
                layoutManager = statisticsLayoutManager
                itemAnimator = DefaultItemAnimator()
                addItemDecoration(
                    StatisticsCardPaddingDecorator(
                        startPadding = R.dimen.spacing_small,
                        cardDistance = R.dimen.spacing_tiny,
                        verticalPadding = R.dimen.spacing_tiny
                    )
                )
            }
            if (resources.isPhone()) {
                PagerSnapHelper().attachToRecyclerView(statisticsRecyclerview)
            }
        }
    }

    override val onBindData: HomeStatisticsScrollcontainerBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        savedStateKey = "stats:${item.stableId}"

        item.data.items.map {
            when (it) {
                is GlobalStatsItem -> GlobalStatisticsCardItem(it, item.onClickListener)
                is AddStatsItem -> AddLocalStatisticsCardItem(it, item.onClickListener)
                is LocalStatsItem -> LocalStatisticsCardItem(it, item.onClickListener, item.onRemoveListener)
            }
        }.let {
            statisticsCardAdapter.update(it)
        }
    }

    override fun onSaveState(): Parcelable? = statisticsLayoutManager.onSaveInstanceState()

    override fun restoreState(state: Parcelable?) {
        if (state != null) {
            statisticsLayoutManager.onRestoreInstanceState(state)
        } else {
            with(viewBinding.value.root.context.resources) {
                val screenWidth = displayMetrics.widthPixels
                val cardWidth = getDimensionPixelSize(R.dimen.statistics_card_width)
                statisticsLayoutManager.scrollToPositionWithOffset(1, (screenWidth - cardWidth) / 2)
            }
        }
    }

    data class Item(
        val data: StatisticsData,
        val onClickListener: (GenericStatsItem) -> Unit,
        val onRemoveListener: (LocalStatsItem) -> Unit = {},
    ) : HomeItem {
        override val stableId: Long = Item::class.java.name.hashCode().toLong()

        // ignore onHelpAction so that view is not re-drawn when only the onHelpAction click listener is updated
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Item

            if (data != other.data) return false
            if (stableId != other.stableId) return false

            return true
        }

        override fun hashCode(): Int {
            return 31 * data.hashCode() + stableId.hashCode()
        }
    }
}
