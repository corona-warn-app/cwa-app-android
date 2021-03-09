package de.rki.coronawarnapp.worker

import de.rki.coronawarnapp.submission.SubmissionSettings
import javax.inject.Inject

@Suppress("UnnecessaryAbstractClass")
abstract class BackgroundWorkSchedulerBase {
    @Inject internal lateinit var submissionSettings: SubmissionSettings
}
