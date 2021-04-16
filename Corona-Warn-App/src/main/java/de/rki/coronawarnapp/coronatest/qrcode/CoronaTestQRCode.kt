package de.rki.coronawarnapp.coronatest.qrcode

import android.os.Parcelable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.util.HashExtensions.toSHA256
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
        val firstName: String?,
        val lastName: String?,
        val dateOfBirth: LocalDate?,
    ) : CoronaTestQRCode() {

        @IgnoredOnParcel
        override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN

        @IgnoredOnParcel
        override val identifier: String
            get() = "hash-${type.raw}-$hash"

        @IgnoredOnParcel
        override val registrationIdentifier: String
            get() = hash.toSHA256()
    }
}

typealias CoronaTestGUID = String
typealias RapidAntigenHash = String
