/******************************************************************************
 * Corona-Warn-App                                                            *
 *                                                                            *
 * SAP SE and all other contributors /                                        *
 * copyright owners license this file to you under the Apache                 *
 * License, Version 2.0 (the "License"); you may not use this                 *
 * file except in compliance with the License.                                *
 * You may obtain a copy of the License at                                    *
 *                                                                            *
 * http://www.apache.org/licenses/LICENSE-2.0                                 *
 *                                                                            *
 * Unless required by applicable law or agreed to in writing,                 *
 * software distributed under the License is distributed on an                *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                     *
 * KIND, either express or implied.  See the License for the                  *
 * specific language governing permissions and limitations                    *
 * under the License.                                                         *
 ******************************************************************************/

package de.rki.coronawarnapp.util.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import org.joda.time.Instant
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import java.io.File
import java.util.UUID

class CommonConverters {
    private val gson = Gson()

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        val listType = object : TypeToken<List<Int?>?>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromIntList(list: List<Int?>?): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toUUID(value: String?): UUID? = value?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toPath(value: String?): File? = value?.let { File(it) }

    @TypeConverter
    fun fromPath(path: File?): String? = path?.path

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromLocalTime(date: LocalTime?): String? = date?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it) }

    @TypeConverter
    fun fromInstant(date: Instant?): String? = date?.toString()

    @TypeConverter
    fun toLocationCode(value: String?): LocationCode? = value?.let { LocationCode(it) }

    @TypeConverter
    fun fromLocationCode(code: LocationCode?): String? = code?.identifier
}
