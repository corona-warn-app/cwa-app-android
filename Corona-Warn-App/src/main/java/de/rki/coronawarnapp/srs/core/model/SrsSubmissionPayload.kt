package de.rki.coronawarnapp.srs.core.model

import de.rki.coronawarnapp.presencetracing.checkins.CheckInsReport
import de.rki.coronawarnapp.server.protocols.external.exposurenotification.TemporaryExposureKeyExportOuterClass
import de.rki.coronawarnapp.server.protocols.internal.SubmissionPayloadOuterClass

data class SrsSubmissionPayload(
    val srsOtp: SrsOtp,
    val checkInsReport: CheckInsReport,
    val visitedCountries: List<String>,
    val submissionType: SubmissionPayloadOuterClass.SubmissionPayload.SubmissionType,
    val exposureKeys: List<TemporaryExposureKeyExportOuterClass.TemporaryExposureKey>
)
