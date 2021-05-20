package de.rki.coronawarnapp.coronatestjournal.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName

@Entity(tableName = "testresults")
@TypeConverters(TestType.Converter::class, TestResult.Converter::class)
data class TestResultEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "testType") val testType: TestType,
    @ColumnInfo(name = "result") val result: TestResult,
    @ColumnInfo(name = "time") val time: Long
)

enum class TestType(val raw: String) {
    @SerializedName("PCR")
    PCR("pcr"),

    @SerializedName("ANTIGEN")
    ANTIGEN("antigen");

    class Converter {
        @TypeConverter
        fun toType(value: String?): TestType? =
            value?.let { TestType.values().single { it.raw == value } }

        @TypeConverter
        fun fromType(type: TestType?): String? = type?.raw
    }
}

enum class TestResult(val raw: String) {
    @SerializedName("POSITIVE") POSITIVE("POSITIVE"),
    @SerializedName("NEGATIVE") NEGATIVE("NEGATIVE");

    class Converter {
        @TypeConverter
        fun toType(value: String?): TestResult? =
            value?.let { TestResult.values().single { it.raw == value } }

        @TypeConverter
        fun fromType(type: TestResult?): String? = type?.raw
    }
}
