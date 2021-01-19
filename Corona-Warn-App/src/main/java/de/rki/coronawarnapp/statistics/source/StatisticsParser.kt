package de.rki.coronawarnapp.statistics.source

import dagger.Reusable
import de.rki.coronawarnapp.server.protocols.internal.stats.StatisticsOuterClass
import de.rki.coronawarnapp.statistics.IncidenceStats
import de.rki.coronawarnapp.statistics.InfectionStats
import de.rki.coronawarnapp.statistics.KeySubmissionsStats
import de.rki.coronawarnapp.statistics.SevenDayRValue
import de.rki.coronawarnapp.statistics.StatisticsData
import de.rki.coronawarnapp.statistics.StatsItem
import org.joda.time.Instant
import timber.log.Timber
import javax.inject.Inject

@Reusable
class StatisticsParser @Inject constructor() {

    fun parse(rawData: ByteArray): StatisticsData {
        val parsed = StatisticsOuterClass.Statistics.parseFrom(rawData)

        if (parsed.cardIdSequenceCount != parsed.keyFigureCardsCount) {
            Timber.tag(TAG).w(
                "Cards have been hidden (sequenceCount=%d != cardCount=%d)",
                parsed.cardIdSequenceCount, parsed.keyFigureCardsCount
            )
        }

        val mappedItems: Set<StatsItem> = parsed.keyFigureCardsList.mapNotNull { rawCard ->
            try {
                val updatedAt = Instant.ofEpochSecond(rawCard.header.updatedAt)
                val keyFigures = rawCard.keyFiguresList
                when (StatsItem.Type.values().singleOrNull { it.id == rawCard.header.cardId }) {
                    StatsItem.Type.INFECTION -> InfectionStats(updatedAt = updatedAt, keyFigures = keyFigures)
                    StatsItem.Type.INCIDENCE -> IncidenceStats(updatedAt = updatedAt, keyFigures = keyFigures)
                    StatsItem.Type.KEYSUBMISSION -> KeySubmissionsStats(updatedAt = updatedAt, keyFigures = keyFigures)
                    StatsItem.Type.SEVEN_DAY_RVALUE -> SevenDayRValue(updatedAt = updatedAt, keyFigures = keyFigures)
                    null -> null.also { Timber.tag(TAG).e("Unknown statistics type: %s", rawCard) }
                }.also {
                    Timber.tag(TAG).v("Parsed %s", it.toString().replace("\n", ", "))
                    it?.requireValidity()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e("Failed to parse raw card: %s", rawCard)
                null
            }
        }.toSet()

        val orderedItems = parsed.cardIdSequenceList.mapNotNull { cardId ->
            mappedItems.singleOrNull { it.cardType.id == cardId }.also {
                if (it == null) Timber.tag(TAG).w("There was no card data for ID=%d", cardId)
            }
        }
        return StatisticsData(items = orderedItems).also {
            Timber.tag(TAG).d("Parsed statistics data, %d cards.", it.items.size)
        }
    }

    companion object {
        const val TAG = "StatisticsParser"
    }
}
