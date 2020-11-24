package de.rki.coronawarnapp.risk.storage.internal

import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import com.google.android.gms.nearby.exposurenotification.ScanInstance
import de.rki.coronawarnapp.risk.storage.internal.windows.toPersistedExposureWindow
import de.rki.coronawarnapp.risk.storage.internal.windows.toPersistedScanInstance
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class PersistedExposureWindowDaoTest : BaseTest() {

    @Test
    fun `mapping is correct`() {
        val window: ExposureWindow = mockk()
        every { window.calibrationConfidence } returns 0
        every { window.dateMillisSinceEpoch } returns 849628347458723L
        every { window.infectiousness } returns 2
        every { window.reportType } returns 2
        window.toPersistedExposureWindow("RESULT_ID").apply {
            riskLevelResultId shouldBe "RESULT_ID"
            dateMillisSinceEpoch shouldBe 849628347458723L
            calibrationConfidence shouldBe 0
            infectiousness shouldBe 2
            reportType shouldBe 2
        }

        val scanInstance: ScanInstance = mockk()
        every { scanInstance.minAttenuationDb } returns 30
        every { scanInstance.secondsSinceLastScan } returns 300
        every { scanInstance.typicalAttenuationDb } returns 25
        scanInstance.toPersistedScanInstance(5000L).apply {
            exposureWindowId shouldBe 5000
            minAttenuationDb shouldBe 30
            typicalAttenuationDb shouldBe 25
            secondsSinceLastScan shouldBe 300
        }
    }
}
