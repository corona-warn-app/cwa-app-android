package de.rki.coronawarnapp.vaccination.core.repository

import de.rki.coronawarnapp.submission.server.SubmissionServer
import de.rki.coronawarnapp.vaccination.core.server.VaccinationValueSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ValueSetsRepository @Inject constructor(
    private val submissionServer: SubmissionServer
) {

    val latestValueSet: Flow<VaccinationValueSet?> = flowOf(null)
}
