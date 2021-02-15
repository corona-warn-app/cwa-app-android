package de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

class ClientMetadataDonorTest : BaseTest() {
    @MockK lateinit var clientVersionWrapper: ClientVersionWrapper
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var enfClient: ENFClient

    private val eTag = "testETag"
    private val enfVersion = 1611L
    private val appVersionCode = 6969420
    private val androidVersionCode = 42L

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { clientVersionWrapper.appVersionCode } returns appVersionCode
        every { clientVersionWrapper.deviceApiLevel } returns androidVersionCode
        coEvery { appConfigProvider.getAppConfig().identifier } returns eTag
        coEvery { enfClient.getENFClientVersion() } returns enfVersion
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createInstance() = ClientMetadataDonor(
        clientVersionParser = ClientVersionParser(),
        clientVersionWrapper = clientVersionWrapper,
        appConfigProvider = appConfigProvider,
        enfClient = enfClient
    )

    @Test
    fun `client metadata is properly collected`() {
        val version = ClientVersionParser().parseClientVersion(appVersionCode).toPPASemanticVersion()

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
