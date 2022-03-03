package de.rki.coronawarnapp.appconfig.sources.fallback

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import com.google.protobuf.UnknownFieldSetLite
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import testhelpers.BaseTest
import testhelpers.EmptyApplication

@Config(sdk = [Build.VERSION_CODES.P], application = EmptyApplication::class)
@RunWith(RobolectricTestRunner::class)
class DefaultAppConfigTest : BaseTest() {

    private val configName = "default_app_config_android.bin"
    private val checkSumName = "default_app_config_android.sha256"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `current default matches checksum`() {
        val config = context.assets.open(configName).readBytes()
        val sha256 = context.assets.open(checkSumName).readBytes().toString(Charsets.UTF_8)
        sha256 shouldBe "b3ad6d66f2b0ff211b112bae3e1eab5e8e019d43e7cd655775c4c4e60b8bf321"
        config.toSHA256() shouldBe sha256
    }

    @Test
    fun `current default config can be parsed`() {
        shouldNotThrowAny {
            val config = context.assets.open(configName).readBytes()
            val parsedConfig = AppConfigAndroid.ApplicationConfigurationAndroid.parseFrom(config)
            parsedConfig shouldNotBe null

            val unknownFields = parsedConfig.javaClass.superclass!!.getDeclaredField("unknownFields").let {
                it.isAccessible = true
                it.get(parsedConfig) as UnknownFieldSetLite
            }
            unknownFields.serializedSize shouldBe 0
        }
    }
}
