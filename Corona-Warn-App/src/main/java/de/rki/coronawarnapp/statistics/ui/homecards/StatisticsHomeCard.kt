package de.rki.coronawarnapp.statistics.ui.homecards

import android.os.Parcelable
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsScrollcontainerBinding
import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.StatsItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.StatisticsCardItem
import de.rki.coronawarnapp.ui.main.home.HomeAdapter
import de.rki.coronawarnapp.ui.main.home.items.HomeItem
import de.rki.coronawarnapp.util.lists.diffutil.update

class StatisticsHomeCard(
    parent: ViewGroup,
    @LayoutRes containerLayout: Int = R.layout.home_statistics_scrollcontainer,
    val restoredState: Parcelable?,
    val onStoreState: (state: Parcelable) -> Unit
) : HomeAdapter.HomeItemVH<StatisticsHomeCard.Item, HomeStatisticsScrollcontainerBinding>(containerLayout, parent) {

    private val statisticsLayoutManager: StatisticsLayoutManager by lazy {
        StatisticsLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private val statisticsCardAdapter by lazy {
        StatisticsCardAdapter()
    }

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
            PagerSnapHelper().attachToRecyclerView(statisticsRecyclerview)
        }
    }

    override fun needsRestore(): Boolean = true

    override val onBindData: HomeStatisticsScrollcontainerBinding.(
        item: Item,
        payloads: List<Any>
    ) -> Unit = { item, _ ->
        item.data.items.map {
            StatisticsCardItem(it, item.onHelpAction)
        }.let {
            statisticsCardAdapter.update(it)
        }
    }

    override fun onSaveState() {
        statisticsLayoutManager.onSaveInstanceState()?.let {
            onStoreState(it)
        }
    }

    override fun onRestoreState() {
        statisticsLayoutManager.onRestoreInstanceState(restoredState)
    }

    data class Item(
        val data: StatisticsData,
        val onHelpAction: (StatsItem) -> Unit
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
