package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import org.joda.time.Duration

interface CovidCertificateConfig {

    val testCertificate: TestCertificate

    interface TestCertificate {
        val waitAfterPublicKeyRegistration: Duration
        val waitForRetry: Duration
    }

    interface Mapper : ConfigMapper<CovidCertificateConfig>
}
