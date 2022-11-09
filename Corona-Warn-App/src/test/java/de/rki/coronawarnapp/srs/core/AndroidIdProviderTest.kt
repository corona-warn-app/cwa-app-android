package de.rki.coronawarnapp.srs.core

import android.content.Context
import android.provider.Settings.Secure
import de.rki.coronawarnapp.srs.core.error.SrsSubmissionException
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class AndroidIdProviderTest : BaseTest() {

    @MockK lateinit var context: Context

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { context.contentResolver } returns mockk(relaxed = true)
        mockkStatic(Secure::class)
    }

    @Test
    fun `Get ANDROID_ID pass`() {
        every { Secure.getString(any(), any()) } returns "14446ff456554c0d"
        shouldNotThrow<SrsSubmissionException> {
            AndroidIdProvider(context).getAndroidId() shouldBe "14446ff456554c0d".decodeHex().toProtoByteString()
        }
    }

    @Test
    fun `Get ANDROID_ID fails`() {
        every { Secure.getString(any(), any()) } returns "Android_ID"
        shouldThrow<SrsSubmissionException> {
            AndroidIdProvider(context).getAndroidId()
        }.errorCode shouldBe SrsSubmissionException.ErrorCode.ANDROID_ID_INVALID_LOCAL
    }
}
