package de.rki.coronawarnapp.submission

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import org.joda.time.LocalDate

@Keep
@Parcelize
data class Symptoms(
    /**
     * this is null if there are no symptoms or there is no information
     */
    val startOfSymptoms: StartOf?,
    val symptomIndication: Indication
) : Parcelable {

    @Keep
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

    @Keep
    @Parcelize
    enum class Indication : Parcelable {
        @SerializedName("POSITIVE")
        POSITIVE,

        @SerializedName("NEGATIVE")
        NEGATIVE,

        @SerializedName("NO_INFORMATION")
        NO_INFORMATION
    }

    companion object {
        val NO_INFO_GIVEN = Symptoms(
            startOfSymptoms = null,
            symptomIndication = Indication.NO_INFORMATION
        )
    }
}
