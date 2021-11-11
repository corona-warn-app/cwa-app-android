package de.rki.coronawarnapp.dccticketing.core.transaction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DccTicketingService(
    val id: String,
    val type: String,
    val serviceEndpoint: String,
    val name: String
) : Parcelable
