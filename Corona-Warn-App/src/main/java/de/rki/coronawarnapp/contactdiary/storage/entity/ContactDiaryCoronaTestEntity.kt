package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryTestEntity.TestType.PCR
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryTestEntity.TestType.ANTIGEN
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryTestEntity.TestResult.POSITIVE
import de.rki.coronawarnapp.contactdiary.storage.entity.ContactDiaryTestEntity.TestResult.NEGATIVE
import org.joda.time.Instant

@Entity(tableName = "corona_tests")
data class ContactDiaryTestEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "testType") val testType: TestType,
    @ColumnInfo(name = "result") val result: TestResult,
    @ColumnInfo(name = "time") val time: Instant
) {
    enum class TestType(val raw: String) {
        @SerializedName("PCR")
        PCR("pcr"),

        @SerializedName("ANTIGEN")
        ANTIGEN("antigen");
    }

    enum class TestResult(val raw: String) {
        @SerializedName("POSITIVE")
        POSITIVE("POSITIVE"),

        @SerializedName("NEGATIVE")
        NEGATIVE("NEGATIVE");
    }
}

fun CoronaTest.canBeAddedToJournal(): Boolean {
    return isViewed && (isNegative || isPositive)
}

fun Map.Entry<CoronaTestGUID, CoronaTest>.asTestResultEntity(): ContactDiaryTestEntity {
    return with(value) {
        ContactDiaryTestEntity(
            id = key,
            testType = if (type == CoronaTest.Type.PCR) PCR else ANTIGEN,
            result = if (isPositive) POSITIVE else NEGATIVE,
            time = when (this) {
                is RACoronaTest -> testedAt
                else -> registeredAt
            }
        )
    }
}
