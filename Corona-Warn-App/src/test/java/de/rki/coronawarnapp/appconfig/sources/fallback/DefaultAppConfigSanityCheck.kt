package de.rki.coronawarnapp.appconfig.sources.fallback

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import testhelpers.BaseTest
import testhelpers.EmptyApplication

@Config(sdk = [Build.VERSION_CODES.P], application = EmptyApplication::class)
@RunWith(RobolectricTestRunner::class)
class DefaultAppConfigSanityCheck : BaseTest() {

    private val legacyConfigName = "default_app_config.bin"
    private val legacyCheckSumName = "default_app_config.sha256"

    private val configName = "default_app_config_android.bin"
    private val checkSumName = "default_app_config_android.sha256"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @After
    fun teardown() {
        clearAllMocks()
    }

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `current default matches checksum`() {
        val config = context.assets.open(configName).readBytes()
        val sha256 = context.assets.open(checkSumName).readBytes().toString(Charsets.UTF_8)
        sha256 shouldBe "827fb746a1128e465d65ec77030fdf38c823dec593ae18aed55195069cf8b701"
        config.toSHA256() shouldBe sha256
    }

    @Test
    fun `current default config can be parsed`() {
        shouldNotThrowAny {
            val config = context.assets.open(configName).readBytes()
            AppConfigAndroid.ApplicationConfigurationAndroid.parseFrom(config) shouldNotBe null
        }
    }

    @Test
    fun `legacy config - current default matches checksum`() {
        val config = context.assets.open(legacyConfigName).readBytes()
        val sha256 = context.assets.open(legacyCheckSumName).readBytes().toString(Charsets.UTF_8)
        sha256 shouldBe "a562bf5940b8c149138634d313db69a298a50e8c52c0b42d18ddf608c385b598"
        config.toSHA256() shouldBe sha256
    }

    @Test
    fun `legacy config - current default config can be parsed`() {
        shouldNotThrowAny {
            val config = context.assets.open(legacyConfigName).readBytes()
            AppConfig.ApplicationConfiguration.parseFrom(config) shouldNotBe null
        }
    }
}
