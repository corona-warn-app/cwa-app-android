package de.rki.coronawarnapp.coronatest.qrcode

import android.os.Parcelable
import de.rki.coronawarnapp.coronatest.TestRegistrationRequest
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.qrcode.scanner.QrCode
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.joda.time.Instant
import org.joda.time.LocalDate

sealed class CoronaTestQRCode : Parcelable, TestRegistrationRequest, QrCode {

    abstract override val type: CoronaTest.Type
    abstract val registrationIdentifier: String
    abstract val rawQrCode: String

    @Parcelize
    data class PCR(
        val qrCodeGUID: CoronaTestGUID,
        override val rawQrCode: String,
        override val isDccConsentGiven: Boolean = false,
        override val dateOfBirth: LocalDate? = null,
    ) : CoronaTestQRCode() {

        @IgnoredOnParcel
        override val isDccSupportedByPoc: Boolean = true

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
        override val rawQrCode: String,
        override val dateOfBirth: LocalDate? = null,
        override val isDccConsentGiven: Boolean = false,
        override val isDccSupportedByPoc: Boolean = false,
        val hash: RapidAntigenHash,
        val createdAt: Instant,
        val firstName: String? = null,
        val lastName: String? = null,
        val testId: String? = null,
        val salt: String? = null,
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
