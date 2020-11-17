package de.rki.coronawarnapp.appconfig.sources.fallback

import android.content.Context
import android.content.res.AssetManager
import de.rki.coronawarnapp.appconfig.ConfigData
import de.rki.coronawarnapp.appconfig.internal.ConfigDataContainer
import de.rki.coronawarnapp.appconfig.mapping.ConfigParser
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.decodeHex
import org.joda.time.Duration
import org.joda.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseIOTest
import java.io.File

class DefaultAppConfigSourceTest : BaseIOTest() {
    @MockK private lateinit var context: Context
    @MockK private lateinit var assetManager: AssetManager
    @MockK lateinit var configParser: ConfigParser
    @MockK lateinit var configData: ConfigData

    private val testDir = File(IO_TEST_BASEDIR, this::class.java.simpleName)
    private val configFile = File(testDir, "default_app_config.bin")

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { context.assets } returns assetManager

        every { assetManager.open("default_app_config.bin") } answers { configFile.inputStream() }

        coEvery { configParser.parse(any()) } returns configData

        testDir.mkdirs()
        testDir.exists() shouldBe true
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
        testDir.deleteRecursively()
    }

    private fun createInstance() = DefaultAppConfigSource(
        context = context,
        configParser = configParser
    )

    @Test
    fun `config loaded from asset`() {
        val testData = "The Cake Is A Lie"
        configFile.writeText(testData)

        val instance = createInstance()
        instance.getRawDefaultConfig() shouldBe testData.toByteArray()
    }

    @Test
    fun `loading internal config data from assets`() = runBlockingTest {
        configFile.writeBytes(APPCONFIG_RAW)

        val instance = createInstance()

        instance.getConfigData() shouldBe ConfigDataContainer(
            serverTime = Instant.EPOCH,
            localOffset = Duration.ZERO,
            mappedConfig = configData,
            configType = ConfigData.Type.LOCAL_DEFAULT,
            identifier = "fallback.local",
            cacheValidity = Duration.ZERO
        )
    }

    @Test
    fun `exceptions when getting the default config are rethrown`() = runBlockingTest {
        val instance = createInstance()

        shouldThrowAny {
            instance.getConfigData()
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
            ).decodeHex().toByteArray()
    }
}
