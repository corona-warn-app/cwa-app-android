package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import de.rki.coronawarnapp.ccl.configuration.model.CCLConfiguration
import de.rki.coronawarnapp.ccl.configuration.model.FunctionDefinition
import de.rki.coronawarnapp.ccl.configuration.model.FunctionParameter
import de.rki.coronawarnapp.ccl.configuration.model.JsonFunctionsDescriptor
import de.rki.coronawarnapp.ccl.configuration.storage.CCLConfigurationRepository
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclCertificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cose
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cwt
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.joda.time.DateTime
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DccWalletInfoCalculationTest : BaseTest() {

    @MockK lateinit var cclConfigurationRepository: CCLConfigurationRepository

    val param1 = FunctionParameter(
        name = "greeting",
        default = TextNode.valueOf("Hello")
    )

    val param2 = FunctionParameter(
        name = "name"
    )

    val jfnLogic = ObjectMapper().readTree(
        """{"return": [{"concatenate": [{"var": "greeting"}," ",{"var": "name"}]}]}"""
    )

    val jfnDescriptor = JsonFunctionsDescriptor(
        name = "greet",
        definition = FunctionDefinition(
            parameters = listOf(param1, param2),
            logic = listOf(jfnLogic)
        )
    )

    private val config = CCLConfiguration(
        identifier = "CCL-DE-0001",
        type = CCLConfiguration.Type.CCLConfiguration,
        country = "DE",
        version = "1.0.0",
        schemaVersion = "1.0.0",
        engine = "JsonFunctions",
        engineVersion = "1.0.0",
        _validFrom = "2021-10-07T00:00:00Z",
        _validTo = "2030-06-01T00:00:00Z",
        logic = CCLConfiguration.Logic(jfnDescriptors = listOf(jfnDescriptor))
    )

    private val dateTime = DateTime.parse("2021-12-30T10:00:00.897+01:00")
    private val defaultInputParameters = CclInputParameters(
        os = "android",
        language = "de",
        now = CclDateTime(dateTime)
    )
    private val qrCode = CoilQrCode("demoQrCode")
    private val issuedAt = Instant.parse("2021-05-16T00:00:00.000Z")
    private val expiresAt = Instant.parse("2021-11-16T00:00:00.000Z")
    val json = "{}"
    private val dccDataVac = mockk<DccData<VaccinationDccV1>>().apply {
        every { kid } returns "kid"
        every { certificateJson } returns json
    }

    private val certificate = mockk<CwaCovidCertificate>().apply {
        every { headerIssuer } returns "Landratsamt Musterstadt"
        every { headerExpiresAt } returns expiresAt
        every { headerIssuedAt } returns issuedAt
        every { qrCodeToDisplay } returns qrCode
        every { getState() } returns CwaCovidCertificate.State.Blocked
        every { dccData } returns dccDataVac
    }

    private lateinit var instance: DccWalletInfoCalculation

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { cclConfigurationRepository.getCCLConfigurations() } returns listOf(config)
        instance = DccWalletInfoCalculation(
            gson = SerializationModule().baseGson(),
            mapper = SerializationModule.jacksonBaseMapper,
            jsonFunctionsWrapper = JsonFunctionsWrapper(
                SerializationModule.jacksonBaseMapper,
                cclConfigurationRepository
            ),
        )
    }

    @Test
    fun `getDccWalletInfoInput mapping works`() {

        val dccWalletInfoInput = instance.getDccWalletInfoInput(
            defaultInputParameters = defaultInputParameters,
            dccList = listOf(certificate)
        )
        dccWalletInfoInput.language shouldBe "de"
        dccWalletInfoInput.os shouldBe "android"

        val cclDateTime = dccWalletInfoInput.now
        cclDateTime.timestamp shouldBe dateTime.millis / 1000
        cclDateTime.localDate shouldBe "2021-12-30"
        cclDateTime.localDateTime shouldBe "2021-12-30T10:00:00+01:00"
        cclDateTime.localDateTimeMidnight shouldBe "2021-12-30T00:00:00+01:00"
        cclDateTime.utcDate shouldBe "2021-12-30"
        cclDateTime.utcDateTime shouldBe "2021-12-30T09:00:00Z"
        cclDateTime.utcDateTimeMidnight shouldBe "2021-12-30T00:00:00Z"

        dccWalletInfoInput.certificates.first() shouldBe CclCertificate(
            barcodeData = qrCode.content,
            cose = Cose("kid"),
            cwt = Cwt(
                iss = "Landratsamt Musterstadt",
                iat = issuedAt.millis / 1000,
                exp = expiresAt.millis / 1000
            ),
            hcert = ObjectMapper().readTree(json),
            validityState = CclCertificate.Validity.BLOCKED
        )
    }
}
