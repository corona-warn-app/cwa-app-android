package de.rki.coronawarnapp.storage.tracing

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "tracing_interval",
    indices = [Index("from", "to")],
    primaryKeys = ["from", "to"]
)
class TracingIntervalEntity {
    var from: Long = 0L
    var to: Long = 0L
}
