package de.rki.coronawarnapp.datadonation.analytics.common

import de.rki.coronawarnapp.server.protocols.internal.ppdd.PpaData
import io.kotest.matchers.shouldBe
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
}
