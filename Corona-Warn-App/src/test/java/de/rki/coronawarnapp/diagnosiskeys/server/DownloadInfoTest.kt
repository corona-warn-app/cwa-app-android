package de.rki.coronawarnapp.diagnosiskeys.server

import io.kotest.matchers.shouldBe
import okhttp3.Headers
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DownloadInfoTest : BaseTest() {

    @Test
    fun `extract server MD5`() {
        val info = DownloadInfo(
            headers = Headers.headersOf("ETag", "\"etag\"")
        )
        info.etag shouldBe "\"etag\""
    }
}
