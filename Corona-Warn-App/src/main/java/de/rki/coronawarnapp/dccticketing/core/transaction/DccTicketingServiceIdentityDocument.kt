package de.rki.coronawarnapp.dccticketing.core.transaction

import com.google.gson.annotations.SerializedName

data class DccTicketingServiceIdentityDocument(
    @SerializedName("id")
    val id: String, // Identifier of the service identity document
    @SerializedName("verificationMethod")
    val verificationMethod: List<DccTicketingVerificationMethod>,    // An array of Verification Method objects (see below.)
    @SerializedName("service")
    val service: List<DccTicketingService>?, // (optional)	An array of Service objects (see below.) As this parameter is optional, it may be defaulted to an empty array.
)
