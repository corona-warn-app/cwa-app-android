package de.rki.coronawarnapp.appconfig

import de.rki.coronawarnapp.appconfig.mapping.ConfigMapper
import okio.ByteString
import org.joda.time.Duration

interface CovidCertificateConfig {

    val testCertificate: TestCertificate

    val expirationThreshold: Duration

    val blockListParameters: List<BlockedChunk>

    val reissueServicePublicKeyDigest: ByteString

    interface TestCertificate {
        val waitAfterPublicKeyRegistration: Duration
        val waitForRetry: Duration
    }

    interface BlockedChunk {
        val indices: List<Int>
        val hash: ByteString
    }

    interface Mapper : ConfigMapper<CovidCertificateConfig>
}
