package de.rki.coronawarnapp.statistics.local.storage

import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import org.joda.time.Instant

sealed class SelectedStatisticsLocation {
    abstract val addedAt: Instant
    abstract val uniqueID: Long

    data class SelectedDistrict(
        val district: Districts.District,
        override val addedAt: Instant,
        override val uniqueID: Long = 1000000L + district.districtId,
    ) : SelectedStatisticsLocation()

    data class SelectedFederalState(
        val federalState: PpaData.PPAFederalState,
        override val addedAt: Instant,
        override val uniqueID: Long = 2000000L + federalState.number
    ) : SelectedStatisticsLocation()
}
