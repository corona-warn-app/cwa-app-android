package de.rki.coronawarnapp.worker

import io.kotest.matchers.shouldBe
import org.joda.time.Duration
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class BackgroundWorkBuilderTest : BaseTest() {

    @Test
    fun `worker interval for key retrieval is 60 minutes, once every hour`() {
        buildDiagnosisKeyRetrievalPeriodicWork().apply {
            workSpec.intervalDuration shouldBe Duration.standardMinutes(60).millis
        }
    }
}
