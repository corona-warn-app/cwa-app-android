package de.rki.coronawarnapp.covidcertificate.common.statecheck

import de.rki.coronawarnapp.ccl.dccwalletinfo.storage.DccWalletInfoRepository
import de.rki.coronawarnapp.covidcertificate.revocation.model.CachedRevocationChunk
import de.rki.coronawarnapp.covidcertificate.revocation.storage.DccRevocationRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DscRepository
import de.rki.coronawarnapp.covidcertificate.signature.core.DscSignatureList
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import testhelpers.coroutines.runBlockingTest2

internal class DccValidityMeasuresObserverTest : BaseTest() {

    @MockK lateinit var dscRepository: DscRepository
    @MockK lateinit var dccWalletInfoRepository: DccWalletInfoRepository
    @MockK lateinit var dccRevocationRepository: DccRevocationRepository

    @MockK lateinit var dscSignatureList: DscSignatureList
    @MockK lateinit var cachedRevocationChunk: CachedRevocationChunk

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { dscRepository.dscSignatureList } returns flowOf(dscSignatureList)
        every { dccWalletInfoRepository.blockedQrCodeHashes } returns flowOf(setOf("hash-1"))
        every { dccRevocationRepository.revocationList } returns flowOf(listOf(cachedRevocationChunk))
    }

    @Test
    fun getDccValidityMeasures() = runBlockingTest2(ignoreActive = true) {
        instance(this).dccValidityMeasures.first() shouldBe DccValidityMeasures(
            dscSignatureList = dscSignatureList,
            blockedQrCodeHashes = setOf("hash-1"),
            revocationList = listOf(cachedRevocationChunk)
        )
    }

    @Test
    fun dccValidityMeasures() = runBlockingTest2(ignoreActive = true) {
        instance(this).dccValidityMeasures() shouldBe DccValidityMeasures(
            dscSignatureList = dscSignatureList,
            blockedQrCodeHashes = setOf("hash-1"),
            revocationList = listOf(cachedRevocationChunk)
        )
    }

    fun instance(scope: CoroutineScope) = DccValidityMeasuresObserver(
        appScope = scope,
        dscRepository = dscRepository,
        dccWalletInfoRepository = dccWalletInfoRepository,
        dccRevocationRepository = dccRevocationRepository
    )
}
