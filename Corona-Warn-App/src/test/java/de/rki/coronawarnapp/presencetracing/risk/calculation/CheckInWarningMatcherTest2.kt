package de.rki.coronawarnapp.presencetracing.risk.calculation

import de.rki.coronawarnapp.presencetracing.checkins.cryptography.CheckInCryptography
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.toTraceLocationIdHash
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass
import de.rki.coronawarnapp.util.encryption.aes.AesCryptography
import de.rki.coronawarnapp.util.toProtoByteString
import io.kotest.matchers.shouldBe
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.decodeHex
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import testhelpers.TestDispatcherProvider
import java.security.SecureRandom
import kotlin.random.asKotlinRandom

@Suppress("MaxLineLength")
internal class CheckInWarningMatcherTest2 : BaseTest() {

    private fun createInstance() = CheckInWarningMatcher(
        TestDispatcherProvider(),
        CheckInCryptography(SecureRandom().asKotlinRandom(), AesCryptography())
    )

    @Test
    fun `deriveTraceWarnings return emptyList when no CheckInReports`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15:00Z",
            endDateStr = "2021-03-04T10:17:00Z"
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

    @Test
    fun `deriveTraceWarnings with failed decoding returns emptyList`() {
        val checkIn1 = createCheckIn(
            id = 2L,
            traceLocationId = "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871",
            startDateStr = "2021-03-04T10:15:00Z",
            endDateStr = "2021-03-04T10:17:00Z"
        )

        val checkInReport1 = CheckInOuterClass.CheckInProtectedReport.newBuilder()
            .setLocationIdHash(
                "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871"
                    .decodeHex().toTraceLocationIdHash().toProtoByteString()
            )
            .build()

        val checkInReport2 = CheckInOuterClass.CheckInProtectedReport.newBuilder()
            .setLocationIdHash(
                "fe84394e73838590cc7707aba0350c130f6d0fb6f0f2535f9735f481dee61871"
                    .decodeHex().toTraceLocationIdHash().toProtoByteString()
            )
            .build()
        createInstance().deriveTraceWarnings(listOf(checkInReport1, checkInReport2), listOf(checkIn1)).size shouldBe 0
    }

    @Test
    fun `deriveTraceWarnings pass`() {
        val checkIn1 = createCheckIn(
            id = 1L,
            traceLocationId = "9baf3a40312f39849f46dad1040f2f039f1cffa1238c41e9db675315cfad39b6",
            startDateStr = "2021-03-04T10:15:00Z",
            endDateStr = "2021-03-04T10:17:00Z"
        )

        val checkIn2 = createCheckIn(
            id = 2L,
            traceLocationId = "03ad6b333d445099c7dfe0ff745ec5cc1330d149ef752f36c3aed4efea13f725",
            startDateStr = "2021-03-04T10:15:00Z",
            endDateStr = "2021-03-04T10:17:00Z"
        )

        val checkInReport1 = CheckInOuterClass.CheckInProtectedReport.parseFrom(
            "CiAZ1gsaWtAA/s9cqffC7ZHMbdVDpRP+ftrdFu45maI48BIQ+VNLZEr+j6qotkv8v1ASlRoQ7v9eQ9R2JtDnU70gn4cidSIgg6KU9FjzawyGA5ZPiZxfgxt8Ou5pSVWqqyAaBkQEA4k=".decodeBase64()!!
                .toByteArray()
        )

        val checkInReport2 = CheckInOuterClass.CheckInProtectedReport.parseFrom(
            "CiAL+oCKqy4KUKKS/lH7yaD4WM2lE8O0MI67WcHZQ1h8tBIQSM6n2ApMmwWCEVwex9yrmBoQXpP2tOktT+RqKNtvv3qbxiIgS62l9vcMeisaFItADTNgTC9a6uEGYkIEL9ISx50LZSo=".decodeBase64()!!
                .toByteArray()
        )
        createInstance().deriveTraceWarnings(
            listOf(checkInReport1, checkInReport2),
            listOf(checkIn1, checkIn2)
        ).size shouldBe 2
    }

    @Test
    fun `deriveTraceWarnings returns emptyList for invalid TraceWarnings`() {
        val checkIn1 = createCheckIn(
            id = 1L,
            traceLocationId = "9baf3a40312f39849f46dad1040f2f039f1cffa1238c41e9db675315cfad39b6",
            startDateStr = "2021-03-04T10:15:00Z",
            endDateStr = "2021-03-04T10:17:00Z"
        )

        val checkIn2 = createCheckIn(
            id = 2L,
            traceLocationId = "03ad6b333d445099c7dfe0ff745ec5cc1330d149ef752f36c3aed4efea13f725",
            startDateStr = "2021-03-04T10:15:00Z",
            endDateStr = "2021-03-04T10:17:00Z"
        )

        val checkInReport1 = CheckInOuterClass.CheckInProtectedReport.parseFrom(
            "CiAZ1gsaWtAA/s9cqffC7ZHMbdVDpRP+ftrdFu45maI48BIQ+VNLZEr+j6qotkv8v1ASlRoQoCFU/onRPTyGHAoQlg3TfyIguFGG1DysdISzetkTdAb4YRKBgEkwnGipMW6gXwNt0oo=".decodeBase64()!!
                .toByteArray()
        )

        val checkInReport2 = CheckInOuterClass.CheckInProtectedReport.parseFrom(
            "CiAL+oCKqy4KUKKS/lH7yaD4WM2lE8O0MI67WcHZQ1h8tBIQSM6n2ApMmwWCEVwex9yrmBoQA8Lb80cx5LhT/uhgsHTI1yIgTcZa3PaEuk2/cHZI4EKC8j3aznAsIhjWAcWNkPyPabk=".decodeBase64()!!
                .toByteArray()
        )

        createInstance().deriveTraceWarnings(
            listOf(checkInReport1, checkInReport2),
            listOf(checkIn1, checkIn2)
        ).size shouldBe 0
    }
}
