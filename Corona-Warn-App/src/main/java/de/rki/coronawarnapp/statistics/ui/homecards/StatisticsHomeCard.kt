package de.rki.coronawarnapp.statistics.ui.homecards

import android.os.Parcelable
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsScrollcontainerBinding
import de.rki.coronawarnapp.statistics.AddStatsItem
import de.rki.coronawarnapp.statistics.GlobalStatsItem
import de.rki.coronawarnapp.statistics.LinkStatsItem
import de.rki.coronawarnapp.statistics.LocalStatsItem
import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.AddLocalStatisticsCardItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.GlobalStatisticsCardItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.LinkCardItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.LocalStatisticsCardItem
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.isPhone
import de.rki.coronawarnapp.util.lists.diffutil.HasPayloadDiffer
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
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val visibleItem = statisticsLayoutManager.findFirstCompletelyVisibleItemPosition()
                        val viewHolder = recyclerView.findViewHolderForAdapterPosition(visibleItem)
                        viewHolder?.itemView?.requestFocus()
                        viewHolder?.itemView?.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                    }
                })
            }
            if (resources.isPhone()) {
                PagerSnapHelper().attachToRecyclerView(statisticsRecyclerview)
            }
        }
    }

    override val onBindData: HomeStatisticsScrollcontainerBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<Item>().lastOrNull() ?: item

        savedStateKey = "stats:${curItem.stableId}"

        curItem.data.items.map {
            when (it) {
                is GlobalStatsItem -> GlobalStatisticsCardItem(it, curItem.onClickListener)
                is AddStatsItem -> AddLocalStatisticsCardItem(it, curItem.onClickListener)
                is LocalStatsItem -> LocalStatisticsCardItem(it, curItem.onClickListener, curItem.onRemoveListener)
                is LinkStatsItem -> LinkCardItem(it, curItem.onClickListener, curItem.openLink)
            }
        }.let {
            statisticsCardAdapter.update(it)
            statisticsCardAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    scrollToCard(2)
                }
            })
        }
    }

    override fun onSaveState(): Parcelable? = statisticsLayoutManager.onSaveInstanceState()

    override fun restoreState(state: Parcelable?) {
        statisticsLayoutManager.onRestoreInstanceState(state)
    }

    override fun onInitialPostBind(): Boolean {
        return if (statisticsCardAdapter.itemCount > 1) {
            scrollToCard()
            true
        } else false // still initial
    }

    private fun scrollToCard(position: Int = 1) {
        with(viewBinding.value.root.context.resources) {
            val screenWidth = displayMetrics.widthPixels
            val cardWidth = getDimensionPixelSize(R.dimen.statistics_card_width)
            statisticsLayoutManager.scrollToPositionWithOffset(position, (screenWidth - cardWidth) / 2)
        }
    }

    data class Item(
        val data: StatisticsData,
        val onClickListener: (StatsItem) -> Unit,
        val onRemoveListener: (LocalStatsItem) -> Unit = {},
        val openLink: (String) -> Unit = {},
    ) : HomeItem, HasPayloadDiffer {

        override val stableId: Long = Item::class.java.name.hashCode().toLong()
    }
}
