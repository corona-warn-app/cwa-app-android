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
    abstract val qrCodeGUID: CoronaTestGUID

    @IgnoredOnParcel
    override val identifier: String
        get() = "qrcode-${type.raw}-$qrCodeGUID"

    @Parcelize
    data class PCR(
        override val qrCodeGUID: CoronaTestGUID,
    ) : CoronaTestQRCode() {

        @IgnoredOnParcel override val type: CoronaTest.Type = CoronaTest.Type.PCR
    }

    @Parcelize
    data class RapidAntigen(
        override val qrCodeGUID: CoronaTestGUID,
        val createdAt: Instant,
        val firstName: String?,
        val lastName: String?,
        val dateOfBirth: LocalDate?,
    ) : CoronaTestQRCode() {

        @IgnoredOnParcel override val type: CoronaTest.Type = CoronaTest.Type.RAPID_ANTIGEN
    }
}

typealias CoronaTestGUID = String
