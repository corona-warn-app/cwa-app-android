package de.rki.coronawarnapp.eventregistration.storage.entity

import de.rki.coronawarnapp.eventregistration.events.TraceLocation
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class EventRegistrationConvertersTest {

    private val converter = EventRegistrationConverters()

    @Test
    fun `toTraceLocationType() should convert different integer values to correct TraceLocation Types`() {
        with(converter) {
            toTraceLocationType(0) shouldBe TraceLocation.Type.UNSPECIFIED
            toTraceLocationType(1) shouldBe TraceLocation.Type.PERMANENT_OTHER
            toTraceLocationType(2) shouldBe TraceLocation.Type.TEMPORARY_OTHER
        }
    }

    @Test
    fun `fromTraceLocationType() should convert TraceLocation Types to correct integer values`() {
        with(converter) {
            fromTraceLocationType(TraceLocation.Type.UNSPECIFIED) shouldBe 0
            fromTraceLocationType(TraceLocation.Type.PERMANENT_OTHER) shouldBe 1
            fromTraceLocationType(TraceLocation.Type.TEMPORARY_OTHER) shouldBe 2
        }
    }
}