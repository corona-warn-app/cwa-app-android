package de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.ApiLevel
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class ClientMetadataDonorTest : BaseTest() {
    @MockK lateinit var apiLevel: ApiLevel
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var enfClient: ENFClient

    @SpyK var clientVersionParser = ClientVersionParser()

    private val eTag = "testETag"
    private val enfVersion = 1611L
    private val appVersionCode = 6969420
    private val androidVersionCode = 42L

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { clientVersionParser.appVersionCode } returns appVersionCode
        every { apiLevel.currentLevel } returns androidVersionCode.toInt()
        every { configData.identifier } returns eTag
        coEvery { appConfigProvider.currentConfig } returns flowOf(configData)
        coEvery { enfClient.getENFClientVersion() } returns enfVersion
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance() = ClientMetadataDonor(
        clientVersionParser = clientVersionParser,
        apiLevel = apiLevel,
        appConfigProvider = appConfigProvider,
        enfClient = enfClient
    )

    @Test
    fun `client metadata is properly collected`() {
        val version = clientVersionParser.parseClientVersion(appVersionCode).toPPASemanticVersion()

        val expectedMetadata = PpaData.PPAClientMetadataAndroid.newBuilder()
            .setAppConfigETag(eTag)
            .setEnfVersion(enfVersion)
            .setCwaVersion(version)
            .setAndroidApiLevel(androidVersionCode)
            .build()

        val parentBuilder = PpaData.PPADataAndroid.newBuilder()

        runBlockingTest2 {
            val contribution = createInstance().beginDonation(object : DonorModule.Request {})
            contribution.injectData(parentBuilder)
            contribution.finishDonation(true)
        }

        val parentProto = parentBuilder.build()

        parentProto.clientMetadata shouldBe expectedMetadata
    }
}
