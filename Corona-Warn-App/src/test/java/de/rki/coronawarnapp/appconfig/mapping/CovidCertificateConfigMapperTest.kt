package de.rki.coronawarnapp.appconfig.mapping

import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.DgcParameters
import io.kotest.matchers.shouldBe
import org.joda.time.Duration
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CovidCertificateConfigMapperTest : BaseTest() {

    private fun createInstance() = CovidCertificateConfigMapper()

    @Test
    fun `values are mapped`() {
        val config = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setDgcParameters(
                DgcParameters.DGCParameters.newBuilder()
                    .setTestCertificateParameters(
                        DgcParameters.DGCTestCertificateParameters.newBuilder()
                            .setWaitForRetryInSeconds(60)
                            .setWaitAfterPublicKeyRegistrationInSeconds(60)
                    )
            )
            .build()
        createInstance().map(config).apply {
            testCertificate.waitAfterPublicKeyRegistration shouldBe Duration.standardSeconds(60)
            testCertificate.waitForRetry shouldBe Duration.standardSeconds(60)
        }
    }

    @Test
    fun `defaults are returned if all dcc parameters are missing`() {
        createInstance().map(AppConfigAndroid.ApplicationConfigurationAndroid.getDefaultInstance()).apply {
            testCertificate.waitAfterPublicKeyRegistration shouldBe Duration.standardSeconds(10)
            testCertificate.waitForRetry shouldBe Duration.standardSeconds(10)
        }
    }

    @Test
    fun `defaults are returned if just test certificate parameters are missing`() {
        val config = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setDgcParameters(DgcParameters.DGCParameters.getDefaultInstance())
            .build()
        createInstance().map(config).apply {
            testCertificate.waitAfterPublicKeyRegistration shouldBe Duration.standardSeconds(10)
            testCertificate.waitForRetry shouldBe Duration.standardSeconds(10)
        }
    }

    @Test
    fun `values are checked for sanity`() {
        val config = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setDgcParameters(
                DgcParameters.DGCParameters.newBuilder()
                    .setTestCertificateParameters(
                        DgcParameters.DGCTestCertificateParameters.newBuilder()
                            .setWaitForRetryInSeconds(61)
                            .setWaitAfterPublicKeyRegistrationInSeconds(61)
                    )
            )
            .build()
        createInstance().map(config).apply {
            testCertificate.waitAfterPublicKeyRegistration shouldBe Duration.standardSeconds(10)
            testCertificate.waitForRetry shouldBe Duration.standardSeconds(10)
        }
    }
}
