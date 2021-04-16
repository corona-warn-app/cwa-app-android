package de.rki.coronawarnapp.presencetracing.storage.entity

import de.rki.coronawarnapp.server.protocols.internal.pt.TraceLocationOuterClass
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

internal class TraceLocationConvertersTest : BaseTest() {

    private val converter = TraceLocationConverters()

    @Test
    fun `toTraceLocationType() should convert different integer values to correct TraceLocation Types`() {
        with(converter) {
            toTraceLocationType(0) shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_UNSPECIFIED
            toTraceLocationType(1) shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER
            toTraceLocationType(2) shouldBe TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER
        }
    }

    @Test
    fun `fromTraceLocationType() should convert TraceLocation Types to correct integer values`() {
        with(converter) {
            fromTraceLocationType(TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_UNSPECIFIED) shouldBe 0
            fromTraceLocationType(TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_PERMANENT_OTHER) shouldBe 1
            fromTraceLocationType(TraceLocationOuterClass.TraceLocationType.LOCATION_TYPE_TEMPORARY_OTHER) shouldBe 2
        }
    }
}
