package de.rki.coronawarnapp.datadonation.analytics.modules.clientmetadata

import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.environment.BuildConfigWrap
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import de.rki.coronawarnapp.util.BuildVersionWrap
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class ClientMetadataDonorTest : BaseTest() {
    @MockK lateinit var appConfigProvider: AppConfigProvider
    @MockK lateinit var configData: ConfigData
    @MockK lateinit var enfClient: ENFClient

    private val eTag = "testETag"
    private val enfVersion = 1611L
    private val androidVersionCode = 42L

    private val versionMajor = 1
    private val versionMinor = 11
    private val versionPatch = 1

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        mockkObject(BuildConfigWrap)
        every { BuildConfigWrap.VERSION_MAJOR } returns versionMajor
        every { BuildConfigWrap.VERSION_MINOR } returns versionMinor
        every { BuildConfigWrap.VERSION_PATCH } returns versionPatch

        mockkObject(BuildVersionWrap)
        every { BuildVersionWrap.SDK_INT } returns androidVersionCode.toInt()
        every { configData.identifier } returns eTag
        coEvery { appConfigProvider.currentConfig } returns flowOf(configData)
        coEvery { enfClient.getENFClientVersion() } returns enfVersion
    }

    private fun createInstance() = ClientMetadataDonor(
        appConfigProvider = appConfigProvider,
        enfClient = enfClient
    )

    @Test
    fun `client metadata is properly collected`() {
        val version = ClientMetadataDonor.ClientVersion().toPPASemanticVersion()

        val expectedMetadata = PpaData.PPAClientMetadataAndroid.newBuilder()
            .setAppConfigETag(eTag)
            .setEnfVersion(enfVersion)
            .setCwaVersion(version)
            .setAndroidApiLevel(androidVersionCode)
            .build()

        val parentBuilder = PpaData.PPADataAndroid.newBuilder()

        runTest {
            val contribution = createInstance().beginDonation(
                object : DonorModule.Request {
                    override val currentConfig: ConfigData = mockk()
                }
            )
            contribution.injectData(parentBuilder)
            contribution.finishDonation(true)
        }

        val parentProto = parentBuilder.build()

        parentProto.clientMetadata shouldBe expectedMetadata
    }
}
