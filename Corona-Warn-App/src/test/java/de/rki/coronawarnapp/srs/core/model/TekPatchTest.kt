package de.rki.coronawarnapp.srs.core.model

import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class TekPatchTest : BaseTest() {

    private val osTek = TemporaryExposureKey.TemporaryExposureKeyBuilder()
        .setKeyData("keydata".toByteArray())
        .setRollingStartIntervalNumber(123)
        .setTransmissionRiskLevel(3)
        .setRollingPeriod(144)
        .setReportType(1)
        .setDaysSinceOnsetOfSymptoms(7)
        .build()

    private val patch = TekPatch.patchFrom(listOf(osTek))

    private val parcelableTek = TekPatch.ParcelableTek(
        keyData = "keydata".toByteArray(),
        rollingStartIntervalNumber = 123,
        transmissionRiskLevel = 3,
        rollingPeriod = 144,
        reportType = 1,
        daysSinceOnsetOfSymptoms = 7
    )

    @Test
    fun `temporary exposure key to persisted tek`() {
        patch.osKeys() shouldBe listOf(osTek)
    }

    @Test
    fun `persisted tek to temporary exposure key`() {
        patch.parcelableTeks shouldBe listOf(parcelableTek)
    }
}
