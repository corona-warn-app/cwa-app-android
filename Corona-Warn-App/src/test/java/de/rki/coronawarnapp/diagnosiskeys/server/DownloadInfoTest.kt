package de.rki.coronawarnapp.diagnosiskeys.server

import io.kotest.matchers.shouldBe
import okhttp3.Headers
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DownloadInfoTest : BaseTest() {

    @Test
    fun `extract server MD5`() {
        val info = DownloadInfo(
            headers = Headers.headersOf("ETAG", "serverMD5"),
            localMD5 = "localMD5"
        )
        info.serverMD5 shouldBe "serverMD5"
        info.localMD5 shouldBe "localMD5"
    }
}
