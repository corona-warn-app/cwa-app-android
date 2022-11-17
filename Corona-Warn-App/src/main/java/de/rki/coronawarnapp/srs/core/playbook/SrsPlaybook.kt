package de.rki.coronawarnapp.srs.core.playbook

import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationFakeRequest
import de.rki.coronawarnapp.srs.core.model.SrsAuthorizationRequest
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionPayload
import de.rki.coronawarnapp.srs.core.server.SrsAuthorizationServer
import de.rki.coronawarnapp.srs.core.server.SrsSubmissionServer
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SrsPlaybook @Inject constructor(
    private val srsSubmissionServer: SrsSubmissionServer,
    private val srsAuthorizationServer: SrsAuthorizationServer,
) {

    suspend fun authorize(request: SrsAuthorizationRequest): Instant {
        return srsAuthorizationServer.authorize(request)
    }

    suspend fun fakeAuthorize(request: SrsAuthorizationFakeRequest) {
        srsAuthorizationServer.fakeAuthorize(request)
    }

    suspend fun submit(payLoad: SrsSubmissionPayload) {
        srsSubmissionServer.submit(payLoad)
    }
}
