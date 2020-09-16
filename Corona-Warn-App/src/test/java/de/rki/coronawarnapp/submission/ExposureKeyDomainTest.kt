package de.rki.coronawarnapp.submission

import KeyExportFormat
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ExposureKeyDomainTest {

    private lateinit var instance: ExposureKeyDomain

    @Before
    fun setup() {
        instance = ExposureKeyDomain()
    }

    @Test
    fun test_limitKeyCount() {
        val rollingStartIntervalNumber = 0
        createKey(rollingStartIntervalNumber)
        Assert.assertEquals(0, instance.limitKeyCount(emptyList<String>()).size)
        Assert.assertEquals(
            7, instance.limitKeyCount(
                listOf(
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "6",
                    "7"
                )
            ).size
        )
        Assert.assertEquals(
            14, instance.limitKeyCount(
                listOf(
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "6",
                    "7",
                    "8",
                    "9",
                    "10",
                    "11",
                    "12",
                    "13",
                    "14",
                    "15"
                )
            ).size
        )
    }

    @Test
    fun test_toSortedHistory() {
        Assert.assertArrayEquals(
            intArrayOf(8, 4, 3, 2, 1), instance.toSortedHistory(
                listOf(
                    createKey(3),
                    createKey(8),
                    createKey(1),
                    createKey(2),
                    createKey(4)
                )
            ).map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
    }

    @Test
    fun test_toExternalFormat() {
        Assert.assertArrayEquals(
            intArrayOf(10, 20), instance.toExternalFormat(
                listOf(
                    createKey(0),
                    createKey(1)
                ),
                TransmissionRiskVector(intArrayOf(0, 1, 2)),
                ::fakeConverter
            ).map { it.rollingStartIntervalNumber }.toTypedArray().toIntArray()
        )
    }

    private fun fakeConverter(key: TemporaryExposureKey, riskValue: Int) =
        KeyExportFormat.TemporaryExposureKey.newBuilder()
            .setRollingStartIntervalNumber(riskValue * 10)
            .build()

    private fun createKey(rollingStartIntervalNumber: Int) =
        TemporaryExposureKey.TemporaryExposureKeyBuilder()
            .setRollingStartIntervalNumber(rollingStartIntervalNumber).build()
}
