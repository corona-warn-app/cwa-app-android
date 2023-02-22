package de.rki.coronawarnapp.di

import android.content.res.AssetManager
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.bugreporting.censors.dcc.DccQrCodeCensor
import de.rki.coronawarnapp.bugreporting.censors.dccticketing.DccTicketingJwtCensor
import de.rki.coronawarnapp.coronatest.qrcode.PcrQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidAntigenQrCodeExtractor
import de.rki.coronawarnapp.coronatest.qrcode.rapid.RapidPcrQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchema
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccJsonSchemaValidator
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccQrCodeExtractor
import de.rki.coronawarnapp.covidcertificate.common.certificate.DccV1Parser
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccCoseDecoder
import de.rki.coronawarnapp.covidcertificate.common.decoder.DccHeaderParser
import de.rki.coronawarnapp.covidcertificate.test.TestCertificateTestData
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationTestData
import de.rki.coronawarnapp.dccticketing.core.qrcode.DccTicketingQrCodeExtractor
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.CheckInQrCodeExtractor
import de.rki.coronawarnapp.qrcode.scanner.QrCodeValidator
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import dgca.verifier.app.engine.DefaultAffectedFieldsDataRetriever
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import io.mockk.every
import io.mockk.mockk
import java.io.FileInputStream
import java.nio.file.Paths

object DiTestProvider {
    val assetManager = mockk<AssetManager>().apply {
        every { open(any()) } answers {
            FileInputStream(Paths.get("src", "main", "assets", arg(0)).toFile())
        }
    }
    val configProvider = mockk<AppConfigProvider>()

    private val schemaValidator = JsonSchemaValidator(SerializationModule.jacksonBaseMapper)

    private val dccJsonSchema = DccJsonSchema(assetManager)
    val dccJsonSchemaValidator = DccJsonSchemaValidator(
        dccJsonSchema = dccJsonSchema,
        schemaValidator = schemaValidator
    )

    val extractor = DccQrCodeExtractor(
        coseDecoder = DccCoseDecoder(AesCryptography()),
        headerParser = DccHeaderParser(),
        bodyParser = DccV1Parser(
            mapper = SerializationModule.jacksonBaseMapper,
            dccJsonSchemaValidator = dccJsonSchemaValidator,
        ),
        censor = DccQrCodeCensor()
    )

    val engine = DefaultCertLogicEngine(
        DefaultAffectedFieldsDataRetriever(
            schemaJsonNode = SerializationModule.jacksonBaseMapper.readTree(dccJsonSchema.rawSchema),
            objectMapper = SerializationModule.jacksonBaseMapper
        ),
        DefaultJsonLogicValidator()
    )

    val dccTicketingQrCodeExtractor = DccTicketingQrCodeExtractor(
        mapper = SerializationModule.jacksonBaseMapper,
        jwtCensor = DccTicketingJwtCensor()
    )
    val qrCodeValidator = QrCodeValidator(
        dccQrCodeExtractor = extractor,
        raExtractor = RapidAntigenQrCodeExtractor(),
        pcrExtractor = PcrQrCodeExtractor(),
        checkInQrCodeExtractor = CheckInQrCodeExtractor(configProvider),
        dccTicketingQrCodeExtractor = dccTicketingQrCodeExtractor,
        rapidPcrQrCodeExtractor = RapidPcrQrCodeExtractor()
    )

    val vaccinationTestData = VaccinationTestData(extractor)
    val testTestData = TestCertificateTestData(extractor)
}
