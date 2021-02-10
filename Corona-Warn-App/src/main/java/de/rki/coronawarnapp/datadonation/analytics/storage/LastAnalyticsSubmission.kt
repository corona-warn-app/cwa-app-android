package de.rki.coronawarnapp.datadonation.analytics.storage

import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import org.joda.time.Instant

data class LastAnalyticsSubmission(
    val timestamp: Instant,
    val ppaDataAndroid: PpaData.PPADataAndroid
)
