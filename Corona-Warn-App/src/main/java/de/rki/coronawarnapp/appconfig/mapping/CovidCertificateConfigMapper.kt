package de.rki.coronawarnapp.appconfig.mapping

import dagger.Reusable
import de.rki.coronawarnapp.appconfig.CovidCertificateConfig
import de.rki.coronawarnapp.appconfig.internal.ApplicationConfigurationInvalidException
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.DgcParameters
import de.rki.coronawarnapp.util.toOkioByteString
import okio.ByteString
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@Reusable
class CovidCertificateConfigMapper @Inject constructor() : CovidCertificateConfig.Mapper {
    override fun map(rawConfig: AppConfigAndroid.ApplicationConfigurationAndroid): CovidCertificateConfig = try {
        if (!rawConfig.hasDgcParameters()) throw IllegalStateException("Config has no DCC parameters.")

        CovidCertificateConfigContainer(
            testCertificate = rawConfig.dgcParameters.mapCovidCertificateConfig(),
            expirationThreshold = rawConfig.dgcParameters.mapExpirationThreshold(),
            blockListParameters = rawConfig.dgcParameters.mapBlockList(),
            reissueServicePublicKeyDigest = rawConfig.dgcParameters.mapReissueServicePublicKeyDigest()
        )
    } catch (e: Exception) {
        throw ApplicationConfigurationInvalidException(
            cause = e,
            message = "Failed to create 'CovidCertificateConfigContainer' from rawConfig=$rawConfig"
        )
    }

    private fun DgcParameters.DGCParameters.mapBlockList(): List<CovidCertificateConfig.BlockedChunk> {
        if (!this.hasBlockListParameters()) {
            Timber.w("DCC config has no blocklist parameters.")
            return emptyList()
        }
        return blockListParameters.blockedUvciChunksList.map {
            BlockedUvciChunk(
                it.indicesList,
                it.hash.toOkioByteString()
            )
        }
    }

    private fun DgcParameters.DGCParameters.mapCovidCertificateConfig(): CovidCertificateConfig.TestCertificate {
        if (!this.hasTestCertificateParameters()) {
            Timber.w("DCC config has no test certificate parameters.")
            return TestCertificateConfigContainer()
        }
        return with(testCertificateParameters) {
            TestCertificateConfigContainer(
                waitAfterPublicKeyRegistration = waitAfterPublicKeyRegistrationInSeconds.let {
                    if (it !in 0..60) {
                        Timber.e("Invalid value for waitAfterPublicKeyRegistration: %s", it)
                        TestCertificateConfigContainer().waitAfterPublicKeyRegistration
                    } else {
                        Duration.standardSeconds(it.toLong())
                    }
                },
                waitForRetry = waitForRetryInSeconds.let {
                    if (it !in 0..60) {
                        Timber.e("Invalid value for waitForRetryInSeconds: %s", it)
                        TestCertificateConfigContainer().waitForRetry
                    } else {
                        Duration.standardSeconds(it.toLong())
                    }
                }
            )
        }
    }

    private fun DgcParameters.DGCParameters.mapExpirationThreshold(): Duration {
        if (this.expirationThresholdInDays == 0) {
            return DEFAULT_EXPIRATION_THRESHOLD
        }

        return Duration.standardDays(expirationThresholdInDays.toLong())
    }

    private fun DgcParameters.DGCParameters.mapReissueServicePublicKeyDigest(): ByteString = try {
        if (reissueServicePublicKeyDigest.isEmpty) throw IllegalStateException("reissueServicePublicKeyDigest is empty")
        reissueServicePublicKeyDigest.toOkioByteString()
    } catch (e: Exception) {
        Timber.w(e, "Failed to map 'reissueServicePublicKeyDigest' from %s", this)
        throw e
    }

    data class CovidCertificateConfigContainer(
        override val testCertificate: CovidCertificateConfig.TestCertificate = TestCertificateConfigContainer(),
        override val expirationThreshold: Duration = DEFAULT_EXPIRATION_THRESHOLD,
        override val blockListParameters: List<CovidCertificateConfig.BlockedChunk> = emptyList(),
        override val reissueServicePublicKeyDigest: ByteString
    ) : CovidCertificateConfig

    data class BlockedUvciChunk(
        override val indices: List<Int>,
        override val hash: ByteString
    ) : CovidCertificateConfig.BlockedChunk

    data class TestCertificateConfigContainer(
        override val waitAfterPublicKeyRegistration: Duration = Duration.standardSeconds(10),
        override val waitForRetry: Duration = Duration.standardSeconds(10),
    ) : CovidCertificateConfig.TestCertificate

    companion object {
        private val DEFAULT_EXPIRATION_THRESHOLD: Duration get() = Duration.standardDays(14)
    }
}
