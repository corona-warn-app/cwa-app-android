package de.rki.coronawarnapp.coronatest.type

import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import org.joda.time.Instant

interface CoronaTest {
    val testGUID: CoronaTestGUID
    val type: Type

    val registeredAt: Instant
    val registrationToken: RegistrationToken

    val isRefreshing: Boolean
    val isSubmissionAllowed: Boolean
    val isSubmitted: Boolean

    enum class Type {
        @SerializedName("PCR")
        PCR,

        @SerializedName("RAPID_ANTIGEN")
        RAPID_ANTIGEN,
    }
}

typealias RegistrationToken = String
