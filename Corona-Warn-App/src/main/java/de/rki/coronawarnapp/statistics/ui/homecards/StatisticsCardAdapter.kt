package de.rki.coronawarnapp.statistics.ui.homecards

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import de.rki.coronawarnapp.statistics.AppliedVaccinationRatesStats
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedCompletelyStats
import de.rki.coronawarnapp.statistics.PersonsVaccinatedOnceStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.ui.homecards.StatisticsCardAdapter.ItemVH
import de.rki.coronawarnapp.statistics.ui.homecards.cards.AddCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.AddLocalStatisticsCardItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.AppliedVaccinationRatesCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.GlobalStatisticsCardItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.IncidenceCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.InfectionsCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.KeySubmissionsCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.LocalIncidenceCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.LocalStatisticsCardItem
import de.rki.coronawarnapp.statistics.ui.homecards.cards.PersonsVaccinatedCompletelyCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.PersonsVaccinatedOnceCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.SevenDayRValueCard
import de.rki.coronawarnapp.statistics.ui.homecards.cards.StatisticsCardItem
import de.rki.coronawarnapp.util.lists.BindableVH
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffUtilAdapter
import de.rki.coronawarnapp.util.lists.diffutil.AsyncDiffer
import de.rki.coronawarnapp.util.lists.modular.ModularAdapter
import de.rki.coronawarnapp.util.lists.modular.mods.DataBinderMod
import de.rki.coronawarnapp.util.lists.modular.mods.StableIdMod
import de.rki.coronawarnapp.util.lists.modular.mods.TypedVHCreatorMod

class StatisticsCardAdapter :
    ModularAdapter<ItemVH<StatisticsCardItem, ViewBinding>>(),
    AsyncDiffUtilAdapter<StatisticsCardItem> {

    override val asyncDiffer: AsyncDiffer<StatisticsCardItem> = AsyncDiffer(adapter = this)

    init {
        listOf(
            StableIdMod(data),
            DataBinderMod<StatisticsCardItem, ItemVH<StatisticsCardItem, ViewBinding>>(data),
            TypedVHCreatorMod({
                val item = data[it]
                item is GlobalStatisticsCardItem && item.stats is InfectionStats
            }) { InfectionsCard(it) },
            TypedVHCreatorMod({
                val item = data[it]
                item is GlobalStatisticsCardItem && item.stats is IncidenceStats
            }) { IncidenceCard(it) },
            TypedVHCreatorMod({
                val item = data[it]
                item is GlobalStatisticsCardItem && item.stats is KeySubmissionsStats
            }) { KeySubmissionsCard(it) },
            TypedVHCreatorMod({
                val item = data[it]
                item is GlobalStatisticsCardItem && item.stats is SevenDayRValue
            }) { SevenDayRValueCard(it) },
            TypedVHCreatorMod({
                val item = data[it]
                item is GlobalStatisticsCardItem && item.stats is PersonsVaccinatedOnceStats
            }) { PersonsVaccinatedOnceCard(it) },
            TypedVHCreatorMod({
                val item = data[it]
                item is GlobalStatisticsCardItem && item.stats is PersonsVaccinatedCompletelyStats
            }) { PersonsVaccinatedCompletelyCard(it) },
            TypedVHCreatorMod({
                val item = data[it]
                item is GlobalStatisticsCardItem && item.stats is AppliedVaccinationRatesStats
            }) { AppliedVaccinationRatesCard(it) },
            TypedVHCreatorMod({
                val item = data[it]
                item is AddLocalStatisticsCardItem
            }) { AddCard(it) },
            TypedVHCreatorMod({
                val item = data[it]
                item is LocalStatisticsCardItem
            }) { LocalIncidenceCard(it) },
        ).let { modules.addAll(it) }
    }

    override fun getItemCount(): Int = data.size

    abstract class ItemVH<Item, VB : ViewBinding>(
        @LayoutRes layoutRes: Int,
        parent: ViewGroup
    ) : ModularAdapter.VH(layoutRes, parent), BindableVH<Item, VB>
}
