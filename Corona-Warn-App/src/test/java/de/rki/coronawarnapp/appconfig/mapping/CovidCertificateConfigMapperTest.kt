package de.rki.coronawarnapp.appconfig.mapping

import com.google.protobuf.ByteString
import de.rki.coronawarnapp.server.protocols.internal.v2.AppConfigAndroid
import de.rki.coronawarnapp.server.protocols.internal.v2.DgcParameters
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.toByteString
import org.joda.time.Duration
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CovidCertificateConfigMapperTest : BaseTest() {

    private fun createInstance() = CovidCertificateConfigMapper()

    private val testReissueServicePublicKeyDigestByteString = "reissueServicePublicKeyDigest".toByteArray()
        .toByteString()
        .sha256()
    private val testReissueServicePublicKeyDigestProtoByteString = testReissueServicePublicKeyDigestByteString
        .toProtoByteString()

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
                    .setExpirationThresholdInDays(13)
                    .setBlockListParameters(
                        DgcParameters.DGCBlocklistParameters.newBuilder()
                            .addBlockedUvciChunks(
                                DgcParameters.DGCBlockedUVCIChunk.newBuilder()
                                    .addIndices(0)
                                    .setHash(
                                        ByteString.copyFrom(
                                            "fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9",
                                            Charsets.UTF_8
                                        )
                                    )
                            )
                            .build()
                    )
                    .setReissueServicePublicKeyDigest(testReissueServicePublicKeyDigestProtoByteString)
            )
            .build()
        createInstance().map(config).apply {
            testCertificate.waitAfterPublicKeyRegistration shouldBe Duration.standardSeconds(60)
            testCertificate.waitForRetry shouldBe Duration.standardSeconds(60)
            expirationThreshold shouldBe Duration.standardDays(13)
            blockListParameters shouldBe listOf(
                CovidCertificateConfigMapper.BlockedUvciChunk(
                    listOf(0),
                    "fcde2b2edba56bf408601fb721fe9b5c338d10ee429ea04fae5511b68fbf8fb9".toByteArray().toByteString()
                )
            )
            reissueServicePublicKeyDigest shouldBe testReissueServicePublicKeyDigestByteString
        }
    }

    /*
    disable temporary

    @Test
    fun `throws if dcc parameters are missing`() {
        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.getDefaultInstance()
        rawConfig.hasDgcParameters() shouldBe false

        shouldThrow<ApplicationConfigurationInvalidException> {
            createInstance().map(rawConfig = rawConfig)
        }.cause should beInstanceOf(IllegalStateException::class)
    }

    @Test
    fun `throws if reissueServicePublicKeyDigest is empty`() {

        val rawConfig = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setDgcParameters(DgcParameters.DGCParameters.getDefaultInstance())
            .build()

        rawConfig.hasDgcParameters() shouldBe true

        shouldThrow<ApplicationConfigurationInvalidException> {
            createInstance().map(rawConfig = rawConfig)
        }.cause should beInstanceOf(IllegalStateException::class)
    }

     */

    @Test
    fun `defaults are returned if test certificate parameters are missing`() {
        val dgcParams = DgcParameters.DGCParameters.newBuilder()
            .setReissueServicePublicKeyDigest(testReissueServicePublicKeyDigestProtoByteString)
        val config = AppConfigAndroid.ApplicationConfigurationAndroid.newBuilder()
            .setDgcParameters(dgcParams)
            .build()

        createInstance().map(config).apply {
            testCertificate.waitAfterPublicKeyRegistration shouldBe Duration.standardSeconds(10)
            testCertificate.waitForRetry shouldBe Duration.standardSeconds(10)
            expirationThreshold shouldBe Duration.standardDays(14)
            blockListParameters shouldBe emptyList()
            reissueServicePublicKeyDigest shouldBe testReissueServicePublicKeyDigestByteString
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
                    .setReissueServicePublicKeyDigest(testReissueServicePublicKeyDigestProtoByteString)
            )
            .build()
        createInstance().map(config).apply {
            testCertificate.waitAfterPublicKeyRegistration shouldBe Duration.standardSeconds(10)
            testCertificate.waitForRetry shouldBe Duration.standardSeconds(10)
            expirationThreshold shouldBe Duration.standardDays(14)
            blockListParameters shouldBe emptyList()
            reissueServicePublicKeyDigest shouldBe testReissueServicePublicKeyDigestByteString
        }
    }
}
