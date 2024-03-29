package de.rki.coronawarnapp.test.submission.ui

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.util.lists.HasStableId
import java.time.Instant

data class TEKHistoryItem(
    val obtainedAt: Instant,
    val key: TemporaryExposureKey
) : HasStableId {

    override val stableId: Long = key.keyData.hashCode().toLong()
}
