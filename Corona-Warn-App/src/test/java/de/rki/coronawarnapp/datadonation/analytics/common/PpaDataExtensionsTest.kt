package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.kotest.matchers.shouldBe
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Instant
import org.joda.time.LocalTime
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
        val now = Instant.now()
        val dayBeforeYesterdayAt2200 = now
            .minus(Days.days(2).toStandardDuration())
            .toDateTime().toLocalDate()
            .toDateTime(LocalTime(22, 0)).toInstant()
        val todayAt0500 = now
            .toDateTime().toLocalDate()
            .toDateTime(LocalTime(5, 0)).toInstant()
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastChangeCheckedRiskLevelTimestamp = dayBeforeYesterdayAt2200,
            testRegisteredAt = todayAt0500
        ) shouldBe 2
    }

    @Test
    fun `days between most recent risk level change and test registration should be 0 if on same day`() {
        val now = Instant.now()
        val todayAt1300 = now
            .toDateTime().toLocalDate()
            .toDateTime(LocalTime(13, 0)).toInstant()
        val todayAt1400 = now.toDateTime().toLocalDate().toDateTime(LocalTime(14, 0)).toInstant()
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastChangeCheckedRiskLevelTimestamp = todayAt1300,
            testRegisteredAt = todayAt1400
        ) shouldBe 0
    }

    @Test
    fun `days should be 0 if lastChangeCheckedRiskLevelTimestamp is null`() {
        val twoHoursAgo = Instant.now().minus(Hours.hours(2).toStandardDuration())
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastChangeCheckedRiskLevelTimestamp = null,
            testRegisteredAt = twoHoursAgo
        ) shouldBe 0
    }

    @Test
    fun `days should be 0 if testRegisteredAt is null`() {
        val twoHoursAgo = Instant.now().minus(Hours.hours(2).toStandardDuration())
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastChangeCheckedRiskLevelTimestamp = twoHoursAgo,
            testRegisteredAt = null
        ) shouldBe 0
    }

    @Test
    fun `days should be 0 if both are null`() {
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastChangeCheckedRiskLevelTimestamp = null,
            testRegisteredAt = null
        ) shouldBe 0
    }

    @Test
    fun `days should be 0 if order is reversed`() {
        val now = Instant.now()
        val twoDaysAgo = now.minus(Days.days(2).toStandardDuration())
        calculateDaysSinceMostRecentDateAtRiskLevelAtTestRegistration(
            lastChangeCheckedRiskLevelTimestamp = now,
            testRegisteredAt = twoDaysAgo
        ) shouldBe 0
    }
}
