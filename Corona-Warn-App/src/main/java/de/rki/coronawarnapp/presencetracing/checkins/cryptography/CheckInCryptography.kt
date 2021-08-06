package de.rki.coronawarnapp.presencetracing.checkins.cryptography

import de.rki.coronawarnapp.presencetracing.checkins.CheckIn
import de.rki.coronawarnapp.presencetracing.checkins.qrcode.TraceLocationId
import de.rki.coronawarnapp.server.protocols.internal.pt.CheckInOuterClass.CheckInProtectedReport
import de.rki.coronawarnapp.server.protocols.internal.pt.TraceWarning
import de.rki.coronawarnapp.util.security.RandomStrong
import javax.inject.Inject
import kotlin.random.Random

class CheckInCryptography @Inject constructor(
    @RandomStrong private val randomSource: Random,
) {
    fun encrypt(
        checkIn: CheckIn,
        transmissionRiskLevel: Int
    ): CheckInProtectedReport {
        // TODO
        throw NotImplementedError()
    }

    fun decrypt(
        checkInProtectedReport: CheckInProtectedReport,
        traceLocationId: TraceLocationId
    ): TraceWarning.TraceTimeIntervalWarning {
        // TODO
        throw NotImplementedError()
    }
}
