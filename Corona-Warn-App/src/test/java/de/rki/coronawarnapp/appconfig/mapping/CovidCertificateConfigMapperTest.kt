package de.rki.coronawarnapp.appconfig.mapping

import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CovidCertificateConfigMapperTest : BaseTest() {

    private fun createInstance() = CovidCertificateConfigMapper()

    @Test
    fun `default values for fallback config are sane`() {
        TODO()
    }

    @Test
    fun `defaults are returned if all dcc parameters are missing`() {
        TODO()
    }

    @Test
    fun `defaults are returned if just test certificate parameters are missing`() {
        TODO()
    }
}
