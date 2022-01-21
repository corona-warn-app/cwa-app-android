package de.rki.coronawarnapp.ccl.dccwalletinfo.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import testhelpers.BaseTest
import timber.log.Timber

internal class DccWalletInfoParserTest : BaseTest() {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun parseDccWalletInfo() {
        javaClass.classLoader!!.getResourceAsStream("ccl/dcc_wallet_info.json").use {
            val walletInfo: DccWalletInfo = ObjectMapper().readValue(it)
            Timber.d("walletInfo=$walletInfo")
        }
    }

    @AfterEach
    fun tearDown() {
    }
}
