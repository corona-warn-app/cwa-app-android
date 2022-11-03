package de.rki.coronawarnapp.appconfig.mapping

import com.google.protobuf.InvalidProtocolBufferException
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.CoronaTestConfig
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.appconfig.LogUploadConfig
import de.rki.coronawarnapp.appconfig.PresenceTracingConfig
import de.rki.coronawarnapp.appconfig.SelfReportSubmissionConfig
import de.rki.coronawarnapp.appconfig.SurveyConfig
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verifySequence
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import java.io.File

class ConfigParserTest : BaseTest() {
    @MockK lateinit var cwaConfigMapper: CWAConfig.Mapper
    @MockK lateinit var keyDownloadConfigMapper: KeyDownloadConfig.Mapper
    @MockK lateinit var exposureDetectionConfigMapper: ExposureDetectionConfig.Mapper
    @MockK lateinit var exposureWindowRiskCalculationConfigMapper: ExposureWindowRiskCalculationConfig.Mapper
    @MockK lateinit var surveyConfigMapper: SurveyConfig.Mapper
    @MockK lateinit var analyticsConfigMapper: AnalyticsConfig.Mapper
    @MockK lateinit var logUploadConfigMapper: LogUploadConfig.Mapper
    @MockK lateinit var presenceTracingConfigMapper: PresenceTracingConfig.Mapper
    @MockK lateinit var coronaTestConfigMapper: CoronaTestConfig.Mapper
    @MockK lateinit var covidCertificateConfigMapper: CovidCertificateConfig.Mapper
    @MockK lateinit var selfReportSubmissionConfigMapper: SelfReportSubmissionConfig.Mapper

    private val appConfig171 = File("src/test/resources/appconfig_1_7_1.bin")
    private val appConfig180 = File("src/test/resources/appconfig_1_8_0.bin")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { cwaConfigMapper.map(any()) } returns mockk()
        every { keyDownloadConfigMapper.map(any()) } returns mockk()
        every { exposureDetectionConfigMapper.map(any()) } returns mockk()
        every { exposureWindowRiskCalculationConfigMapper.map(any()) } returns mockk()
        every { surveyConfigMapper.map(any()) } returns mockk()
        every { analyticsConfigMapper.map(any()) } returns mockk()
        every { logUploadConfigMapper.map(any()) } returns mockk()
        every { presenceTracingConfigMapper.map(any()) } returns mockk()
        every { coronaTestConfigMapper.map(any()) } returns mockk()
        every { covidCertificateConfigMapper.map(any()) } returns mockk()
        every { selfReportSubmissionConfigMapper.map(any()) } returns mockk()

        appConfig171.exists() shouldBe true
        appConfig180.exists() shouldBe true
    }

    private fun createInstance(): ConfigParser = ConfigParser(
        cwaConfigMapper = cwaConfigMapper,
        keyDownloadConfigMapper = keyDownloadConfigMapper,
        exposureDetectionConfigMapper = exposureDetectionConfigMapper,
        exposureWindowRiskCalculationConfigMapper = exposureWindowRiskCalculationConfigMapper,
        surveyConfigMapper = surveyConfigMapper,
        analyticsConfigMapper = analyticsConfigMapper,
        logUploadConfigMapper = logUploadConfigMapper,
        presenceTracingConfigMapper = presenceTracingConfigMapper,
        coronaTestConfigMapper = coronaTestConfigMapper,
        covidCertificateConfigMapper = covidCertificateConfigMapper,
        selfReportSubmissionConfigMapper = selfReportSubmissionConfigMapper,
    )

    @Test
    fun `simple init`() {
        createInstance().parse(APPCONFIG_RAW.toByteArray()).apply {

            verifySequence {
                cwaConfigMapper.map(any())
                keyDownloadConfigMapper.map(any())
                exposureDetectionConfigMapper.map(any())
                exposureWindowRiskCalculationConfigMapper.map(any())
                surveyConfigMapper.map(any())
            }
        }
    }

    @Test
    fun `parsing the 1_7_1 config throws exception`() {
        // We don't want to use the 1.7.x config as fallback once we are on 1.8.x
        // If parsing cached config data fails, we will fallback to the default config of 1.8.x
        shouldThrow<InvalidProtocolBufferException> {
            createInstance().parse(appConfig171.readBytes())
        }
    }

    @Test
    fun `parsing the 1_8_0 config does not throw an exception`() {
        createInstance().parse(appConfig180.readBytes()).apply {
            this shouldNotBe null
        }
    }

    companion object {
        private val APPCONFIG_RAW = (
            "081f101f1a0e0a0c0a0872657365727665641001220244452a061" +
                "8c20320e003320508061084073ad4010a0d0a0b1900000000004" +
                "0524020010a0d120b190000000000002440200112140a1209000" +
                "000000000f03f1900000000000000401a160a0b1900000000008" +
                "04b40200111000000000000f03f1a1f0a14090000000000804b4" +
                "0190000000000804f40200111000000000000e03f220f0a0b190" +
                "000000000002e402001100122160a12090000000000002e40190" +
                "00000008087c34010022a0f0a0b190000000000002e402001100" +
                "12a160a12090000000000002e4019000000008087c3401002320" +
                "a10041804200328023001399a9999999999c93f420c0a0408011" +
                "0010a04080210024a750a031e32461220000000000000f03f000" +
                "000000000f03f000000000000f03f000000000000f03f220b080" +
                "111000000000000f03f220b080211000000000000f03f320b080" +
                "111000000000000f03f320b080211000000000000f03f320b080" +
                "311000000000000f03f320b080411000000000000f03f"
            ).decodeHex()
    }
}
