package de.rki.coronawarnapp.covidcertificate.person.ui.admission.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdmissionScenarios(
    val title: String,
    val scenarios: List<AdmissionScenario>,
    val scenariosAsJson: String
) : Parcelable

@Parcelize
data class AdmissionScenario(
    val identifier: String,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
) : Parcelable
