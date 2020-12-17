package de.rki.coronawarnapp.contactdiary.retention

import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import dagger.Reusable
import de.rki.coronawarnapp.worker.BackgroundConstants
import org.joda.time.DateTimeConstants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class ContactDiaryWorkBuilder @Inject constructor() {

    fun buildPeriodicWork(): PeriodicWorkRequest = PeriodicWorkRequestBuilder<ContactDiaryRetentionWorker>(
        DateTimeConstants.HOURS_PER_DAY.toLong(), TimeUnit.HOURS
    )
        .setInitialDelay(
            BackgroundConstants.KIND_DELAY,
            TimeUnit.MINUTES
        )
        .build()
}
