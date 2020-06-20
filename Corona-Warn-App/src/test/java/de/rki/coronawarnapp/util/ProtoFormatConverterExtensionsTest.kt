package de.rki.coronawarnapp.util

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import de.rki.coronawarnapp.util.ProtoFormatConverterExtensions.transformKeyHistoryToExternalFormat
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test

private const val DEFAULT_TRANSMISSION_RISK_LEVEL = 1
private const val TRANSMISSION_RISK_DAY_0 = 5
private const val TRANSMISSION_RISK_DAY_1 = 6
private const val TRANSMISSION_RISK_DAY_2 = 8
private const val TRANSMISSION_RISK_DAY_3 = 8
private const val TRANSMISSION_RISK_DAY_4 = 8
private const val TRANSMISSION_RISK_DAY_5 = 5
private const val TRANSMISSION_RISK_DAY_6 = 3
private const val TRANSMISSION_RISK_DAY_7 = 1

class ProtoFormatConverterExtensionsTest {

    @Test
    fun areTransmissionRiskLevelsCorrectlyAssigned() {

        val key1 = byteArrayOf(
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
        )

        val diagnosisKeyList = mutableListOf<TemporaryExposureKey>()
        val numKeys = 13
        for (pos in 0 until numKeys) {
            diagnosisKeyList.add(
                TemporaryExposureKey.TemporaryExposureKeyBuilder()
                    .setKeyData(key1)
                    .setRollingStartIntervalNumber(pos * 144)
                    .setRollingPeriod(144)
                    .setTransmissionRiskLevel(0)
                    .build()
            )
        }

        val transformedKeyList = diagnosisKeyList.transformKeyHistoryToExternalFormat()
            .sortedWith(compareBy { it.rollingStartIntervalNumber })

        MatcherAssert.assertThat(
            transformedKeyList.size,
            CoreMatchers.equalTo(numKeys)
        )

        val correctRiskLevels = arrayOf(
            DEFAULT_TRANSMISSION_RISK_LEVEL,
            DEFAULT_TRANSMISSION_RISK_LEVEL,
            DEFAULT_TRANSMISSION_RISK_LEVEL,
            DEFAULT_TRANSMISSION_RISK_LEVEL,
            DEFAULT_TRANSMISSION_RISK_LEVEL,
            DEFAULT_TRANSMISSION_RISK_LEVEL,
            TRANSMISSION_RISK_DAY_7,
            TRANSMISSION_RISK_DAY_6,
            TRANSMISSION_RISK_DAY_5,
            TRANSMISSION_RISK_DAY_4,
            TRANSMISSION_RISK_DAY_3,
            TRANSMISSION_RISK_DAY_2,
            TRANSMISSION_RISK_DAY_1
        )

        for (pos in 0 until numKeys) {
            val key = transformedKeyList[pos]
            MatcherAssert.assertThat(
                key.transmissionRiskLevel,
                CoreMatchers.equalTo(correctRiskLevels[pos])
            )
            MatcherAssert.assertThat(
                key.rollingStartIntervalNumber,
                CoreMatchers.equalTo(pos * 144)
            )
        }
    }

    @Test
    fun areTransmissionRiskLevelsCorrectlyAssignedWithOnlyOneKey() {

        val key1 = byteArrayOf(
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
        )

        val diagnosisKeyList = mutableListOf<TemporaryExposureKey>()
        val numKeys = 1
        for (pos in 0 until numKeys) {
            diagnosisKeyList.add(
                TemporaryExposureKey.TemporaryExposureKeyBuilder()
                    .setKeyData(key1)
                    .setRollingStartIntervalNumber(pos * 144)
                    .setRollingPeriod(144)
                    .setTransmissionRiskLevel(0)
                    .build()
            )
        }

        val transformedKeyList = diagnosisKeyList.transformKeyHistoryToExternalFormat()
            .sortedWith(compareBy { it.rollingStartIntervalNumber })

        MatcherAssert.assertThat(
            transformedKeyList.size,
            CoreMatchers.equalTo(numKeys)
        )

        val correctRiskLevels = arrayOf(
            TRANSMISSION_RISK_DAY_1
        )

        for (pos in 0 until numKeys) {
            val key = transformedKeyList[pos]
            MatcherAssert.assertThat(
                key.transmissionRiskLevel,
                CoreMatchers.equalTo(correctRiskLevels[pos])
            )
            MatcherAssert.assertThat(
                key.rollingStartIntervalNumber,
                CoreMatchers.equalTo(pos * 144)
            )
        }
    }
}
