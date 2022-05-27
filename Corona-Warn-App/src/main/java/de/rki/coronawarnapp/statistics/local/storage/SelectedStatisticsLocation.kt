package de.rki.coronawarnapp.statistics.local.storage

import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import java.time.Instant

sealed class SelectedStatisticsLocation {
    abstract val addedAt: Instant
    abstract val uniqueID: Long

    data class SelectedDistrict(
        val district: Districts.District,
        override val addedAt: Instant,
    ) : SelectedStatisticsLocation() {
        override val uniqueID: Long
            get() = 1000000L + district.districtId
    }

    data class SelectedFederalState(
        val federalState: PpaData.PPAFederalState,
        override val addedAt: Instant,
    ) : SelectedStatisticsLocation() {
        override val uniqueID: Long
            get() = 2000000L + federalState.number
    }
}
