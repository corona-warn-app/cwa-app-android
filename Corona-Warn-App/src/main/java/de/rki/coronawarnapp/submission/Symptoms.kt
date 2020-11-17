package de.rki.coronawarnapp.submission

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.LocalDate

@Parcelize
data class Symptoms(
    /**
     * this is null if there are no symptoms or there is no information
     */
    val startOfSymptoms: StartOf?,
    val symptomIndication: Indication
) : Parcelable {

    sealed class StartOf : Parcelable {
        @Parcelize
        data class Date(val date: LocalDate) : StartOf()

        @Parcelize
        object LastSevenDays : StartOf()

        @Parcelize
        object OneToTwoWeeksAgo : StartOf()

        @Parcelize
        object MoreThanTwoWeeks : StartOf()

        @Parcelize
        object NoInformation : StartOf()
    }

    @Parcelize
    enum class Indication : Parcelable {
        POSITIVE,
        NEGATIVE,
        NO_INFORMATION
    }

    companion object {
        val NO_INFO_GIVEN = Symptoms(
            startOfSymptoms = null,
            symptomIndication = Indication.NO_INFORMATION
        )
    }
}
