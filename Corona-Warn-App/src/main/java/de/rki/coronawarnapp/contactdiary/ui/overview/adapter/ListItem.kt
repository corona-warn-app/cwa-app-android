package de.rki.coronawarnapp.contactdiary.ui.overview.adapter

import de.rki.coronawarnapp.risk.result.AggregatedRiskPerDateResult
import org.joda.time.LocalDate

data class ListItem(
    val date: LocalDate
) {
    val data: MutableList<Data> = mutableListOf()
    var riskLevel: AggregatedRiskPerDateResult? = null

    data class Data(
        val drawableId: Int,
        val text: String
    )
}
