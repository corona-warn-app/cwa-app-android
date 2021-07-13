package de.rki.coronawarnapp.statistics.local.storage

import de.rki.coronawarnapp.datadonation.analytics.common.Districts
import org.joda.time.Instant

data class SelectedDistrict(
    val district: Districts.District,
    val addedAt: Instant,
)
