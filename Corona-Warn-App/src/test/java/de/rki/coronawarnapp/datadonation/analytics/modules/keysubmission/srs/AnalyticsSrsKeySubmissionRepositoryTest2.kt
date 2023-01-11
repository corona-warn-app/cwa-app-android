package de.rki.coronawarnapp.datadonation.analytics.modules.keysubmission.srs

import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData.PPAKeySubmissionType
import de.rki.coronawarnapp.srs.core.model.SrsSubmissionType
import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Test

import testhelpers.BaseTest

internal class AnalyticsSrsKeySubmissionRepositoryTest2 : BaseTest() {

    @Test
    fun toPpaSubmissionType() {
        SrsSubmissionType.SRS_SELF_TEST.toPpaSubmissionType() shouldBe
            PPAKeySubmissionType.SUBMISSION_TYPE_SRS_SELF_TEST

        SrsSubmissionType.SRS_REGISTERED_RAT.toPpaSubmissionType() shouldBe
            PPAKeySubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_RAT

        SrsSubmissionType.SRS_UNREGISTERED_RAT.toPpaSubmissionType() shouldBe
            PPAKeySubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_RAT

        SrsSubmissionType.SRS_REGISTERED_PCR.toPpaSubmissionType() shouldBe
            PPAKeySubmissionType.SUBMISSION_TYPE_SRS_REGISTERED_PCR

        SrsSubmissionType.SRS_UNREGISTERED_PCR.toPpaSubmissionType() shouldBe
            PPAKeySubmissionType.SUBMISSION_TYPE_SRS_UNREGISTERED_PCR

        SrsSubmissionType.SRS_RAPID_PCR.toPpaSubmissionType() shouldBe
            PPAKeySubmissionType.SUBMISSION_TYPE_SRS_RAPID_PCR

        SrsSubmissionType.SRS_OTHER.toPpaSubmissionType() shouldBe
            PPAKeySubmissionType.SUBMISSION_TYPE_SRS_OTHER
    }
}
