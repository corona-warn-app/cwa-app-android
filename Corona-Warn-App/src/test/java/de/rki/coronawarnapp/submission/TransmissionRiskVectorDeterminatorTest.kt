package de.rki.coronawarnapp.submission

import io.mockk.mockk
import org.junit.Assert
import org.junit.Test

class TransmissionRiskVectorDeterminatorTest {

    @Test
    fun test_determine() {
        val submissionStatusRepository = mockk<SubmissionStatusRepository>()

        Assert.assertArrayEquals(
            intArrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator(submissionStatusRepository).determine(
                Symptoms(
                    null, Symptoms.Indication.NO_INFORMATION
                )
            ).raw
        )
    }

    @Test
    fun test_numberOfDays() {
        Assert.assertEquals(
            4,
            TransmissionRiskVectorDeterminator.numberOfDays(
                0,
                1000 * 3600 * (24 * 4 + 2)))
    }
}
