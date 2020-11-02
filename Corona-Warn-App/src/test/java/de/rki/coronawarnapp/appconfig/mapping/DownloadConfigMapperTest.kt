package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.AppConfig
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class DownloadConfigMapperTest : BaseTest() {
    private fun createInstance() = DownloadConfigMapper()

    @Test
    fun `simple creation`() {
        val rawConfig = AppConfig.ApplicationConfiguration.newBuilder()
            .build()
        createInstance().map(rawConfig).apply {

        }
    }
}
