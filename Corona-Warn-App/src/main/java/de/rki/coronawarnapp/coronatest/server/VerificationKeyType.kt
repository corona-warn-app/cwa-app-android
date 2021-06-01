package de.rki.coronawarnapp.coronatest.server

import com.google.gson.annotations.SerializedName

enum class VerificationKeyType {
    @SerializedName("GUID")
    GUID,

    @SerializedName("TELETAN")
    TELETAN;
}
