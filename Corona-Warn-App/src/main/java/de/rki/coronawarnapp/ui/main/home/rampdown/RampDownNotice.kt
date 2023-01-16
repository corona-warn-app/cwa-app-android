package de.rki.coronawarnapp.ui.main.home.rampdown

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RampDownNotice(
    val visible: Boolean,
    val title: String,
    val subtitle: String,
    val description: String,
    val faqUrl: String?,
) : Parcelable
