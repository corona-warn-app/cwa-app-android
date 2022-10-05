package de.rki.coronawarnapp.presencetracing.locations.server

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.ConscryptMode
import testhelpers.BaseTest
import testhelpers.EmptyApplication

@Config(sdk = [Build.VERSION_CODES.P], application = EmptyApplication::class)
@RunWith(RobolectricTestRunner::class)
@ConscryptMode(ConscryptMode.Mode.OFF)
class DefaultQrCodePosterTemplateTest : BaseTest() {

    private val templateName = "default_qr_code_poster_template_android.bin"
    private val checkSumName = "default_qr_code_poster_template_android.sha256"

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `current default matches checksum`() {
        val template = context.assets.open(templateName).readBytes()
        val sha256 = context.assets.open(checkSumName).readBytes().toString(Charsets.UTF_8)
        sha256 shouldBe "1e972018100828aa63bc2559713cffa22d8db62f3ce56b3edbe72ad8cb7adb16"
        template.toSHA256() shouldBe sha256
    }
}
