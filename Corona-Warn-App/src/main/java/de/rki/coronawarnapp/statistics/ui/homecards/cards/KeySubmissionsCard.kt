package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsKeysubmissionsLayoutBinding
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter

class KeySubmissionsCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<StatisticsCardItem, HomeStatisticsCardsKeysubmissionsLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout, parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsKeysubmissionsLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsKeysubmissionsLayoutBinding.(
        item: StatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        item.stats as KeySubmissionsStats
    }
}
