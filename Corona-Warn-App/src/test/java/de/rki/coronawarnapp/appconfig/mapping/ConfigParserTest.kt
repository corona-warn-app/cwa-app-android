package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
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
    @MockK lateinit var exposureWindowRiskCalculationConfigMapper: ExposureWindowRiskCalculationConfig.Mapper

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { cwaConfigMapper.map(any()) } returns mockk()
        every { keyDownloadConfigMapper.map(any()) } returns mockk()
        every { exposureDetectionConfigMapper.map(any()) } returns mockk()
        every { exposureWindowRiskCalculationConfigMapper.map(any()) } returns mockk()
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    private fun createInstance(): ConfigParser = ConfigParser(
        cwaConfigMapper = cwaConfigMapper,
        keyDownloadConfigMapper = keyDownloadConfigMapper,
        exposureDetectionConfigMapper = exposureDetectionConfigMapper,
        exposureWindowRiskCalculationConfigMapper = exposureWindowRiskCalculationConfigMapper
    )

    @Test
    fun `simple init`() {
        createInstance().parse(APPCONFIG_RAW.toByteArray()).apply {

            verifySequence {
                cwaConfigMapper.map(any())
                keyDownloadConfigMapper.map(any())
                exposureDetectionConfigMapper.map(any())
                exposureWindowRiskCalculationConfigMapper.map(any())
            }
        }
    }

    companion object {
        private val APPCONFIG_RAW = (
            "504b030414000808080057416b510000000000000000000000000a0000006578706f72742e736967018a0075ff0a87010a380a1864652e726b692e636f726f6e617761726e6170702d6465761a02763122033236322a13312e322e3834302e31303034352e342e332e3210011801224730450221008eb5ccdedca6e3a5628635e2e6fcb49805bf7de76c9d86df5bd8ed8a3d6474c802205d3aee38a775f53233bc0f5ef744cb3073644a402156cfe6ffd1cc2037b929cc504b0708a4ac96eb8f0000008a000000504b030414000808080057416b510000000000000000000000000a0000006578706f72742e62696ee390179097e2e3e2e1e2284a2d4e2d2a4b4d116054627271d5629338c4acf080d98895834da085ddea0a23172f17b72403083804392800b942502e830a902b24c225c409e17eb0878a33384889c1f43478031509c21448c9738970c2c4a10afc110a1ed82bf1c33432e801c5816e12839baf07d1d1d07ed84180490b5da1160e85465c022c122c0acc1a4c068c96b36682c0497b271e2e160ea02620c924c0e455cac52c67e426a40073262e5a899b03e117208709ce3142963142936146e6b0c03900504b0708cb9d508cc300000083010000504b0102140014000808080057416b51a4ac96eb8f0000008a0000000a00000000000000000000000000000000006578706f72742e736967504b0102140014000808080057416b51cb9d508cc3000000830100000a00000000000000000000000000c70000006578706f72742e62696e504b0506000000000200020070000000c20100000000"
            ).decodeHex()
    }
}
