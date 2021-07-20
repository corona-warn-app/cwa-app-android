package de.rki.coronawarnapp.statistics.local.storage

import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import org.joda.time.Instant

sealed class SelectedStatisticsLocation {
    abstract val addedAt: Instant
    abstract val uniqueID: Int

    data class SelectedDistrict(
        val district: Districts.District,
        override val addedAt: Instant,
        override val uniqueID: Int = district.districtId,
    ) : SelectedStatisticsLocation()

    data class SelectedFederalState(
        val federalState: PpaData.PPAFederalState,
        override val addedAt: Instant,
        override val uniqueID: Int = federalState.number
    ) : SelectedStatisticsLocation()
}
