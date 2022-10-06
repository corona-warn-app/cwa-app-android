package de.rki.coronawarnapp.statistics.ui.homecards.cards

import android.view.ViewGroup
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.databinding.HomeStatisticsCardsLinkCardLayoutBinding
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter

class LinkViewCard(parent: ViewGroup) :
    StatisticsCardAdapter.ItemVH<LinkCardItem, HomeStatisticsCardsLinkCardLayoutBinding>(
        R.layout.home_statistics_cards_basecard_layout,
        parent
    ) {

    override val viewBinding = lazy {
        HomeStatisticsCardsLinkCardLayoutBinding.inflate(
            layoutInflater,
            itemView.findViewById(R.id.card_container),
            true
        )
    }

    override val onBindData: HomeStatisticsCardsLinkCardLayoutBinding.(
        item: LinkCardItem,
        payloads: List<Any>
    ) -> Unit = { item, payloads ->
        val curItem = payloads.filterIsInstance<LinkCardItem>().lastOrNull() ?: item
        infoStatistics.setOnClickListener { curItem.onClickListener(curItem.linkStats) }
        linkButton.setOnClickListener { curItem.openLink(curItem.linkStats.url) }
    }
}
