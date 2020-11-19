package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.appconfig.RiskCalculationConfig
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifySequence
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ConfigParserTest : BaseTest() {
    @MockK lateinit var cwaConfigMapper: CWAConfig.Mapper
    @MockK lateinit var keyDownloadConfigMapper: KeyDownloadConfig.Mapper
    @MockK lateinit var exposureDetectionConfigMapper: ExposureDetectionConfig.Mapper
    @MockK lateinit var riskCalculationConfigMapper: RiskCalculationConfig.Mapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { cwaConfigMapper.map(any()) } returns mockk()
        every { keyDownloadConfigMapper.map(any()) } returns mockk()
        every { exposureDetectionConfigMapper.map(any()) } returns mockk()
        every { riskCalculationConfigMapper.map(any()) } returns mockk()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): ConfigParser = ConfigParser(
        cwaConfigMapper = cwaConfigMapper,
        keyDownloadConfigMapper = keyDownloadConfigMapper,
        exposureDetectionConfigMapper = exposureDetectionConfigMapper,
        riskCalculationConfigMapper = riskCalculationConfigMapper
    )

    @Test
    fun `simple init`() {
        createInstance().parse(APPCONFIG_RAW.toByteArray()).apply {

            verifySequence {
                cwaConfigMapper.map(any())
                keyDownloadConfigMapper.map(any())
                exposureDetectionConfigMapper.map(any())
                riskCalculationConfigMapper.map(any())
            }
        }
    }

    companion object {
        private val APPCONFIG_RAW = (
            "080b124d0a230a034c4f57180f221a68747470733a2f2f777777" +
                "2e636f726f6e617761726e2e6170700a260a0448494748100f1848221a68747470733a2f2f7777772e636f7" +
                "26f6e617761726e2e6170701a640a10080110021803200428053006380740081100000000000049401a0a20" +
                "0128013001380140012100000000000049402a1008051005180520052805300538054005310000000000003" +
                "4403a0e1001180120012801300138014001410000000000004940221c0a040837103f121209000000000000" +
                "f03f11000000000000e03f20192a1a0a0a0a041008180212021005120c0a0408011804120408011804"
            ).decodeHex()
    }
}
