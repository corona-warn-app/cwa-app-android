package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData

import io.kotest.matchers.shouldBe
import java.time.Instant
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PpaDataExtensionsTest : BaseTest() {

    @Test
    fun `federal state to short code mapping`() {
        PpaData.PPAFederalState.FEDERAL_STATE_BW.federalStateShortName shouldBe "BW"
        PpaData.PPAFederalState.FEDERAL_STATE_BY.federalStateShortName shouldBe "BY"
        PpaData.PPAFederalState.FEDERAL_STATE_BE.federalStateShortName shouldBe "BE"
        PpaData.PPAFederalState.FEDERAL_STATE_BB.federalStateShortName shouldBe "BB"
        PpaData.PPAFederalState.FEDERAL_STATE_HB.federalStateShortName shouldBe "HB"
        PpaData.PPAFederalState.FEDERAL_STATE_HH.federalStateShortName shouldBe "HH"
        PpaData.PPAFederalState.FEDERAL_STATE_HE.federalStateShortName shouldBe "HE"
        PpaData.PPAFederalState.FEDERAL_STATE_MV.federalStateShortName shouldBe "MV"
        PpaData.PPAFederalState.FEDERAL_STATE_NI.federalStateShortName shouldBe "NI"
        PpaData.PPAFederalState.FEDERAL_STATE_NRW.federalStateShortName shouldBe "NW"
        PpaData.PPAFederalState.FEDERAL_STATE_RP.federalStateShortName shouldBe "RP"
        PpaData.PPAFederalState.FEDERAL_STATE_SL.federalStateShortName shouldBe "SL"
        PpaData.PPAFederalState.FEDERAL_STATE_SN.federalStateShortName shouldBe "SN"
        PpaData.PPAFederalState.FEDERAL_STATE_ST.federalStateShortName shouldBe "ST"
        PpaData.PPAFederalState.FEDERAL_STATE_SH.federalStateShortName shouldBe "SH"
        PpaData.PPAFederalState.FEDERAL_STATE_TH.federalStateShortName shouldBe "TH"
    }

    @Test
    fun `days since most recent date at risk level at test registration are calculated correctly`() {
        val march15At2200 = Instant.parse("2021-03-15T22:00:00.000Z").toLocalDateUtc()
        val march17At0500 = Instant.parse("2021-03-17T05:00:00.000Z").toLocalDateUtc()
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastDateAtRiskLevel = march15At2200,
            testRegisteredAt = march17At0500
        ) shouldBe 2
    }

    @Test
    fun `days between most recent risk level change and test registration should be 0 if on same day`() {
        val march15At0500 = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDateUtc()
        val march15At2200 = Instant.parse("2021-03-15T22:00:00.000Z").toLocalDateUtc()
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastDateAtRiskLevel = march15At0500,
            testRegisteredAt = march15At2200
        ) shouldBe 0
    }

    @Test
    fun `days should be -1 if lastChangeCheckedRiskLevelTimestamp is null`() {
        val march15At0500 = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDateUtc()
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastDateAtRiskLevel = null,
            testRegisteredAt = march15At0500
        ) shouldBe -1
    }

    @Test
    fun `days should be -1 if testRegisteredAt is null`() {
        val march15At0500 = Instant.parse("2021-03-15T05:00:00.000Z").toLocalDateUtc()
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastDateAtRiskLevel = march15At0500,
            testRegisteredAt = null
        ) shouldBe -1
    }

    @Test
    fun `days should be -1 if both are null`() {
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastDateAtRiskLevel = null,
            testRegisteredAt = null
        ) shouldBe -1
    }

    @Test
    fun `days should be -1 if order is reversed`() {
        val march20At2200 = Instant.parse("2021-03-20T22:00:00.000Z").toLocalDateUtc()
        val march10At0500 = Instant.parse("2021-03-10T05:00:00.000Z").toLocalDateUtc()
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastDateAtRiskLevel = march20At2200,
            testRegisteredAt = march10At0500
        ) shouldBe -1
    }
}
