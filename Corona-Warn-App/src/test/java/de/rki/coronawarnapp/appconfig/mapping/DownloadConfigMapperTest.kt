package de.rki.coronawarnapp.appconfig.mapping

import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DownloadConfigMapperTest : BaseTest() {
    private fun createInstance() = DownloadConfigMapper()

    @Test
    fun `parse etag missmatch for hours`() {
        TODO()
//        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
//            .build()
//        createInstance().map(rawConfig).apply {
//
//        }
    }

    @Test
    fun `parse etag missmatch for days`() {
        TODO()
    }
}
