package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.DgcParameters
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CovidCertificateConfigMapper @Inject constructor() : CovidCertificateConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): CovidCertificateConfig {
        if (!rawConfig.hasDgcParameters()) {
            Timber.w("Config has no DCC parameters.")
            return CovidCertificateConfigContainer()
        }

        return CovidCertificateConfigContainer(
            testCertificate = rawConfig.dgcParameters.mapCovidCertificateConfig()
        )
    }

    private fun DgcParameters.DGCParameters.mapCovidCertificateConfig(): CovidCertificateConfig.TestCertificate {
        if (!this.hasTestCertificateParameters()) {
            Timber.w("DCC config has no test certificate parameters.")
            return TestCertificateConfigContainer()
        }
        return with(testCertificateParameters) {
            TestCertificateConfigContainer(
                waitAfterPublicKeyRegistration = waitAfterPublicKeyRegistrationInSeconds.let {
                    if (it > 60 || it < 0) {
                        Timber.e("Invalid value for waitAfterPublicKeyRegistration: %s", it)
                        TestCertificateConfigContainer().waitAfterPublicKeyRegistration
                    } else {
                        Duration.standardSeconds(it.toLong())
                    }
                },
                waitForRetry = waitForRetryInSeconds.let {
                    if (it > 60 || it < 0) {
                        Timber.e("Invalid value for waitForRetryInSeconds: %s", it)
                        TestCertificateConfigContainer().waitForRetry
                    } else {
                        Duration.standardSeconds(it.toLong())
                    }
                }
            )
        }
    }

    data class CovidCertificateConfigContainer(
        override val testCertificate: CovidCertificateConfig.TestCertificate = TestCertificateConfigContainer()
    ) : CovidCertificateConfig

    data class TestCertificateConfigContainer(
        override val waitAfterPublicKeyRegistration: Duration = Duration.standardSeconds(10),
        override val waitForRetry: Duration = Duration.standardSeconds(10),
    ) : CovidCertificateConfig.TestCertificate
}
