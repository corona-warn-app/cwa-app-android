package de.rki.coronawarnapp.coronatest.qrcode

import android.os.Parcelable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.joda.time.Instant
import org.joda.time.LocalDate

sealed class CoronaTestQRCode : Parcelable, TestRegistrationRequest {

    abstract override val type: CoronaTest.Type
    abstract val registrationIdentifier: String

    @Parcelize
    data class PCR(
        val qrCodeGUID: CoronaTestGUID,
    ) : CoronaTestQRCode() {

        @IgnoredOnParcel
        override val type: CoronaTest.Type = CoronaTest.Type.PCR

        @IgnoredOnParcel
        override val identifier: String
            get() = "qrcode-${type.raw}-$qrCodeGUID"

        @IgnoredOnParcel
        override val registrationIdentifier: String
            get() = qrCodeGUID
    }

    @Parcelize
    data class RapidAntigen(
        val hash: RapidAntigenHash,
        val createdAt: Instant,
        val firstName: String? = null,
        val lastName: String? = null,
        val dateOfBirth: LocalDate? = null,
        val testid: String? = null,
        val salt: String? = null
    ) : CoronaTestQRCode() {

        @IgnoredOnParcel
        override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

        @IgnoredOnParcel
        override val identifier: String
            get() = "qrcode-${type.raw}-$hash"

        @IgnoredOnParcel
        override val registrationIdentifier: String
            // We hash in the VerificationServer.retrieveRegistrationToken which was needed anyway for PCR
            get() = hash
    }
}

typealias CoronaTestGUID = String
typealias RapidAntigenHash = String
