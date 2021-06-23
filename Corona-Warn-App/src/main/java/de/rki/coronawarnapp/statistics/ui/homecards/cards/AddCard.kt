package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsAddLayoutBinding
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter

class AddCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<AllStatisticsCardItem, HomeStatisticsCardsAddLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsAddLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsAddLayoutBinding.(
        item: AllStatisticsCardItem,
        payloads: List<Any>
    ) -> Unit = { item, _ ->

    }
}
