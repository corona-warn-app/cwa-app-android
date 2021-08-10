package de.rki.coronawarnapp.covidcertificate.signature.core.storage

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.covidcertificate.signature.core.DscDataParser
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import testhelpers.BaseTest
import testhelpers.EmptyApplication

@Config(sdk = [Build.VERSION_CODES.P], application = EmptyApplication::class)
@RunWith(RobolectricTestRunner::class)
internal class DefaultDscSourceTest : BaseTest() {

    private val dscListName = "default_dsc_list.bin"
    private val checkSumName = "default_dsc_list.sha256"

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `current default data matches checksum`() {
        val dscList = context.assets.open(dscListName).readBytes()
        val sha256 = context.assets.open(checkSumName).readBytes().toString(Charsets.UTF_8)
        sha256 shouldBe "d3fd1479bc17c036501ffcf692bdf04cf4131828d2e135ccf003e69ac6522ee1"
        dscList.toSHA256() shouldBe sha256
    }

    @Test
    fun `current default data can be parsed`() {
        shouldNotThrowAny {
            val rawDscList = context.assets.open(dscListName).readBytes()
            val parsedDscList = DscDataParser().parse(rawDscList)
            parsedDscList shouldNotBe null
        }
    }
}
