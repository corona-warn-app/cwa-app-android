package de.rki.coronawarnapp.test.submission.ui

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.util.lists.HasStableId
import org.joda.time.Instant

data class TEKHistoryItem(
    val obtainedAt: Instant,
    val batchId: String,
    val key: TemporaryExposureKey
) : HasStableId {

    override val stableId: Long = key.keyData.hashCode().toLong()
}
