package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import okio.ByteString
import java.time.Duration

interface CovidCertificateConfig {

    val testCertificate: TestCertificate

    val expirationThreshold: Duration

    val reissueServicePublicKeyDigest: ByteString

    interface TestCertificate {
        val waitAfterPublicKeyRegistration: Duration
        val waitForRetry: Duration
    }

    interface Mapper : ConfigMapper<CovidCertificateConfig>
}
