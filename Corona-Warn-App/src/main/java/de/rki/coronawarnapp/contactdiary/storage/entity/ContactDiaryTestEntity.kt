package de.rki.coronawarnapp.contactdiary.storage.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import de.rki.coronawarnapp.coronatest.qrcode.CoronaTestGUID
import de.rki.coronawarnapp.coronatest.type.CoronaTest
import de.rki.coronawarnapp.coronatest.type.rapidantigen.RACoronaTest
import org.joda.time.Instant

@Entity(tableName = "tests")
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
            testType = if (type == CoronaTest.Type.PCR)
                ContactDiaryTestEntity.TestType.PCR
            else
                ContactDiaryTestEntity.TestType.ANTIGEN,
            result = if (isPositive)
                ContactDiaryTestEntity.TestResult.POSITIVE
            else
                ContactDiaryTestEntity.TestResult.NEGATIVE,
            time = when (this) {
                is RACoronaTest -> testedAt
                else -> registeredAt
            }
        )
    }
}
