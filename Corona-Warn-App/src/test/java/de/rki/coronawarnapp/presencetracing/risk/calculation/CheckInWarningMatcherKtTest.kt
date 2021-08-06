package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class CheckInWarningMatcherKtTest : BaseTest() {

    @Test
    fun `TraceTimeIntervalWarning validity`() {
        TraceWarning.TraceTimeIntervalWarning.newBuilder()
            .build()
            .isValid() shouldBe false

        TraceWarning.TraceTimeIntervalWarning.newBuilder()
            .setPeriod(0)
            .build()
            .isValid() shouldBe false

        TraceWarning.TraceTimeIntervalWarning.newBuilder()
            .setStartIntervalNumber(-1)
            .setPeriod(1)
            .setTransmissionRiskLevel(1)
            .build()
            .isValid() shouldBe false

        TraceWarning.TraceTimeIntervalWarning.newBuilder()
            .setStartIntervalNumber(1)
            .setPeriod(1)
            .setTransmissionRiskLevel(0)
            .build()
            .isValid() shouldBe false

        TraceWarning.TraceTimeIntervalWarning.newBuilder()
            .setStartIntervalNumber(1)
            .setPeriod(1)
            .setTransmissionRiskLevel(9)
            .build()
            .isValid() shouldBe false

        TraceWarning.TraceTimeIntervalWarning.newBuilder()
            .setStartIntervalNumber(0)
            .setPeriod(1)
            .setTransmissionRiskLevel(1)
            .build()
            .isValid() shouldBe true

        TraceWarning.TraceTimeIntervalWarning.newBuilder()
            .setStartIntervalNumber(1)
            .setPeriod(1)
            .setTransmissionRiskLevel(1)
            .build()
            .isValid() shouldBe true
    }
}
