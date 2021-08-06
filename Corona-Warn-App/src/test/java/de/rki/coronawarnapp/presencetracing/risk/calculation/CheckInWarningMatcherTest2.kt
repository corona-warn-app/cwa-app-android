package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.covidcertificate.common.cryptography.AesCryptography
import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import org.junit.jupiter.api.Test
import testhelpers.TestDispatcherProvider
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

internal class CheckInWarningMatcherTest2 {

    private fun createInstance() = CheckInWarningMatcher(
        TestDispatcherProvider(),
        CheckInCryptography(SecureRandom().asKotlinRandom(), AesCryptography())
    )

    @Test
    fun `deriveTraceWarnings return emptyList when no CheckInReports`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15+01:00",
            endDateStr = "2021-03-04T10:17+01:00"
        )

        createInstance().deriveTraceWarnings(emptyList(), listOf(checkIn1)).size shouldBe 0
    }

    @Test
    fun `deriveTraceWarnings return emptyList when no CheckIns`() {
        val checkInReport = CheckInOuterClass.CheckInProtectedReport.newBuilder()
            .setLocationIdHash("fe84394e73838590cc7707aba0350c".decodeBase64()!!.toProtoByteString())
            .build()
        createInstance().deriveTraceWarnings(listOf(checkInReport), emptyList()).size shouldBe 0
    }
}
