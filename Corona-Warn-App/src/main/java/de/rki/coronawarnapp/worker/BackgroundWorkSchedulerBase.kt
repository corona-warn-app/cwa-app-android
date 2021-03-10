package de.rki.coronawarnapp.worker

import de.rki.coronawarnapp.storage.TracingSettings
import de.rki.coronawarnapp.submission.SubmissionSettings
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class BackgroundWorkSchedulerBase {
    @Inject internal lateinit var submissionSettings: dagger.Lazy<SubmissionSettings>
    @Inject internal lateinit var tracingSettings: dagger.Lazy<TracingSettings>
}
