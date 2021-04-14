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
    abstract val guid: CoronaTestGUID

    @IgnoredOnParcel
    override val identifier: String
        get() = "qrcode-${type.raw}-$guid"

    @Parcelize
    data class PCR(
        override val type: CoronaTest.Type,
        override val guid: CoronaTestGUID,
    ) : CoronaTestQRCode()

    @Parcelize
    data class RapidAntigen(
        override val type: CoronaTest.Type,
        override val guid: CoronaTestGUID,
        val createdAt: Instant,
        val firstName: String?,
        val lastName: String?,
        val dateOfBirth: LocalDate?,
    ) : CoronaTestQRCode()
}

typealias CoronaTestGUID = String
