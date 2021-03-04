package de.rki.coronawarnapp.util

import androidx.core.net.toUri
import io.kotest.matchers.shouldBe
import org.junit.Test
import testhelpers.BaseTestInstrumentation

class UriTest : BaseTestInstrumentation() {

    @Test
    fun navUriConvertsSchemeAndAuthorityToLowercase() {
        val uri = "HTTPS://CORONAWARN.APP/E1/SOME_PATH_GOES_HERE".toUri()
        uri.navUri.toString() shouldBe "https://coronawarn.app/E1/SOME_PATH_GOES_HERE"

        val uri2 = "HTTPS://CORONAWARN.APP/e1/some_path_goes_here".toUri()
        uri2.navUri.toString() shouldBe "https://coronawarn.app/e1/some_path_goes_here"
    }

    @Test
    fun navUriDoesNotChangePath() {
        val uri = "https://coronawarn.app/E1/SOME_PATH_GOES_HERE".toUri()
        uri.navUri.toString() shouldBe "https://coronawarn.app/E1/SOME_PATH_GOES_HERE"

        val uri2 = "https://coronawarn.app/e1/some_path_goes_here".toUri()
        uri2.navUri.toString() shouldBe "https://coronawarn.app/e1/some_path_goes_here"
    }

    @Test
    fun navUriConvertsSchemeAndAuthorityToLowercaseWithWWW() {
        val uri = "HTTPS://WWW.CORONAWARN.APP/E1/SOME_PATH_GOES_HERE".toUri()
        uri.navUri.toString() shouldBe "https://www.coronawarn.app/E1/SOME_PATH_GOES_HERE"

        val uri2 = "HTTPS://WWW.CORONAWARN.APP/e1/some_path_goes_here".toUri()
        uri2.navUri.toString() shouldBe "https://www.coronawarn.app/e1/some_path_goes_here"
    }

    @Test
    fun navUriDoesNotChangePathWithWWW() {
        val uri = "https://www.coronawarn.app/E1/SOME_PATH_GOES_HERE".toUri()
        uri.navUri.toString() shouldBe "https://www.coronawarn.app/E1/SOME_PATH_GOES_HERE"

        val uri2 = "https://www.coronawarn.app/e1/some_path_goes_here".toUri()
        uri2.navUri.toString() shouldBe "https://www.coronawarn.app/e1/some_path_goes_here"
    }
}
