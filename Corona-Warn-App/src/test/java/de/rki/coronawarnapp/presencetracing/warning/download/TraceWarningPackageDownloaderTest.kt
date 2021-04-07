package de.rki.coronawarnapp.presencetracing.warning.download

import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class TraceWarningPackageDownloaderTest : BaseTest() {

    @Test
    fun `errors during writeProtoBufToFile cause download to be marked as failed`() {
        // TODO
    }
}
