package de.rki.coronawarnapp.di

import android.content.res.AssetManager
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.PresenceTracingConfigContainer
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.mapping.ConfigMapping
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
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass
import de.rki.coronawarnapp.server.protocols.internal.v2.PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor.PayloadEncoding
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import de.rki.coronawarnapp.util.serialization.SerializationModule
import de.rki.coronawarnapp.util.serialization.validation.JsonSchemaValidator
import dgca.verifier.app.engine.DefaultAffectedFieldsDataRetriever
import dgca.verifier.app.engine.DefaultCertLogicEngine
import dgca.verifier.app.engine.DefaultJsonLogicValidator
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.io.FileInputStream
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant

object DiTestProvider {
    private val assetManager = mockk<AssetManager>().apply {
        every { open(any()) } answers {
            FileInputStream(Paths.get("src", "main", "assets", arg(0)).toFile())
        }
    }
    private val configProvider = mockk<AppConfigProvider>().apply {
        every { currentConfig } returns flowOf(
            ConfigDataContainer(
                serverTime = Instant.parse("2020-11-03T05:35:16.000Z"),
                localOffset = Duration.ZERO,
                mappedConfig = mockk<ConfigMapping>().apply {
                    every { isDeviceTimeCheckEnabled } returns true
                    every { presenceTracing } returns PresenceTracingConfigContainer(
                        qrCodeDescriptors = listOf(
                            PresenceTracingParametersOuterClass.PresenceTracingQRCodeDescriptor.newBuilder()
                                .setVersionGroupIndex(0)
                                .setEncodedPayloadGroupIndex(1)
                                .setPayloadEncoding(PayloadEncoding.BASE64)
                                .setRegexPattern("https://e\\.coronawarn\\.app\\?v=(\\d+)\\#(.+)")
                                .build()
                        )
                    )
                },
                identifier = "identifier",
                configType = ConfigData.Type.FROM_SERVER,
                cacheValidity = Duration.ofMinutes(5)
            )
        )
    }

    private val schemaValidator = JsonSchemaValidator(SerializationModule.jacksonBaseMapper)

    private val dccJsonSchema = DccJsonSchema(assetManager)
    private val dccJsonSchemaValidator = DccJsonSchemaValidator(
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
