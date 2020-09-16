package de.rki.coronawarnapp.submission

import org.junit.Assert
import org.junit.Test

class TransmissionRiskVectorDeterminatorTest {

    @Test
    fun test_determine() {
        Assert.assertArrayEquals(
            intArrayOf(5, 6, 7, 7, 7, 6, 4, 3, 2, 1, 1, 1, 1, 1, 1),
            TransmissionRiskVectorDeterminator().determine(
                Symptoms(
                    null, SymptomIndication.NO_INFORMATION
                )
            ).raw
        )
    }
}
