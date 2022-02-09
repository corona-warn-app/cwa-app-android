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
        override val isDccConsentGiven: Boolean = false,
        override val dateOfBirth: LocalDate? = null,
        override val rawQrCode: String
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

    abstract class Rapid : CoronaTestQRCode() {
        abstract val hash: RapidHash
        abstract val createdAt: Instant
        abstract val firstName: String?
        abstract val lastName: String?
        abstract val testId: String?
        abstract val salt: String?

        override val identifier: String
            get() = "qrcode-${type.raw}-$hash"

        override val registrationIdentifier: String
            // We hash in the VerificationServer.retrieveRegistrationToken which was needed anyway for PCR
            get() = hash
    }

    @Parcelize
    data class RapidAntigen(
        override val dateOfBirth: LocalDate? = null,
        override val isDccConsentGiven: Boolean = false,
        override val isDccSupportedByPoc: Boolean = false,
        override val rawQrCode: String,
        override val hash: RapidAntigenHash,
        override val createdAt: Instant,
        override val firstName: String? = null,
        override val lastName: String? = null,
        override val testId: String? = null,
        override val salt: String? = null,
    ) : Rapid() {
        @IgnoredOnParcel
        override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN
    }

    @Parcelize
    data class RapidPCR(
        override val dateOfBirth: LocalDate? = null,
        override val isDccConsentGiven: Boolean = false,
        override val isDccSupportedByPoc: Boolean = false,
        override val rawQrCode: String,
        override val hash: RapidPCRHash,
        override val createdAt: Instant,
        override val firstName: String? = null,
        override val lastName: String? = null,
        override val testId: String? = null,
        override val salt: String? = null,
    ) : Rapid() {
        // TODO. Change to RAPID_PCR
        @IgnoredOnParcel
        override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN
    }
}

typealias CoronaTestGUID = String
typealias RapidHash = String
typealias RapidAntigenHash = String
typealias RapidPCRHash = String
