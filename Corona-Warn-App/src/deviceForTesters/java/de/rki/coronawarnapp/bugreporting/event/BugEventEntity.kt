package de.rki.coronawarnapp.bugreporting.event

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.Instant

@Entity
data class BugEventEntity(
    override val createdAt: Instant = Instant.now(),
    override var tag: String? = null,
    override val info: String? = null,
    override val exceptionClass: String,
    override val exceptionMessage: String? = null,
    override val stackTrace: String,
    override val appVersionName: String,
    override val appVersionCode: Int,
    override val apiLevel: Int,
    override val androidVersion: String,
    override val shortID: String,
    override val logHistory: List<String>
) : BugEvent {
    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0
}
