package de.rki.coronawarnapp.submission

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

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
        object LastSevenDays : StartOf() {
            override fun equals(other: Any?): Boolean = other is LastSevenDays

            override fun hashCode(): Int = System.identityHashCode(this)
        }

        @Parcelize
        object OneToTwoWeeksAgo : StartOf() {
            override fun equals(other: Any?): Boolean = other is OneToTwoWeeksAgo

            override fun hashCode(): Int = System.identityHashCode(this)
        }

        @Parcelize
        object MoreThanTwoWeeks : StartOf() {
            override fun equals(other: Any?): Boolean = other is MoreThanTwoWeeks

            override fun hashCode(): Int = System.identityHashCode(this)
        }

        @Parcelize
        object NoInformation : StartOf() {
            override fun equals(other: Any?): Boolean = other is NoInformation

            override fun hashCode(): Int = System.identityHashCode(this)
        }
    }

    @Parcelize
    enum class Indication : Parcelable {
        @JsonProperty("POSITIVE")
        POSITIVE,

        @JsonProperty("NEGATIVE")
        NEGATIVE,

        @JsonProperty("NO_INFORMATION")
        NO_INFORMATION
    }

    companion object {
        val NO_INFO_GIVEN = Symptoms(
            startOfSymptoms = null,
            symptomIndication = Indication.NO_INFORMATION
        )
    }
}
