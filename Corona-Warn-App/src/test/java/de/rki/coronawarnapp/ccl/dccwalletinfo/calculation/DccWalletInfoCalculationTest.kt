package de.rki.coronawarnapp.ccl.dccwalletinfo.calculation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.rki.coronawarnapp.ccl.configuration.model.CclDateTime
import de.rki.coronawarnapp.ccl.configuration.model.CclInputParameters
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.CclCertificate
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cose
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.Cwt
import de.rki.coronawarnapp.ccl.dccwalletinfo.model.DccWalletInfo
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccData
import de.rki.coronawarnapp.covidcertificate.common.certificate.VaccinationDccV1
import de.rki.coronawarnapp.util.TimeAndDateExtensions.seconds
import de.rki.coronawarnapp.util.qrcode.coil.CoilQrCode
import de.rki.coronawarnapp.util.serialization.SerializationModule
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import org.joda.time.DateTime
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider

class DccWalletInfoCalculationTest : BaseTest() {

    @MockK lateinit var walletInfo: DccWalletInfo
    @MockK lateinit var cclJsonFunctions: CclJsonFunctions
    @MockK lateinit var mapper: ObjectMapper

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
        coEvery { cclJsonFunctions.evaluateFunction("getDccWalletInfo", any()) } returns NullNode.instance
        every { mapper.treeToValue(any(), DccWalletInfo::class.java) } returns walletInfo
        every { mapper.readTree(any<String>()) } returns ObjectNode(JsonNodeFactory.instance)
        every { mapper.valueToTree<JsonNode>(any()) } returns NullNode.instance

        instance = DccWalletInfoCalculation(
            gson = SerializationModule().baseGson(),
            mapper = mapper,
            cclJsonFunctions = cclJsonFunctions,
            dispatcherProvider = TestDispatcherProvider()
        )
    }

    @Test
    fun `getDccWalletInfoInput works`() {

        val dccWalletInfoInput = instance.getDccWalletInfoInput(
            defaultInputParameters = defaultInputParameters,
            dccList = listOf(certificate),
            boosterNotificationRules = NullNode.instance,
            scenarioIdentifier = "",
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
                iat = issuedAt.seconds,
                exp = expiresAt.seconds
            ),
            hcert = ObjectMapper().readTree(json),
            validityState = CclCertificate.Validity.BLOCKED
        )
    }

    @Test
    fun `execution works`() = runBlockingTest {
        instance.getDccWalletInfo(
            dccList = listOf(certificate),
            dateTime = dateTime
        ) shouldBe walletInfo
    }
}
