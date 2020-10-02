package de.rki.coronawarnapp.diagnosiskeys.storage.legacy

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "date",
    indices = [Index("id")]
)
class KeyCacheLegacyEntity {
    @PrimaryKey
    var id: String = ""

    var path: String = ""

    var type: Int = 0
}
