package de.rki.coronawarnapp.bugreporting.event

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.Instant
import java.util.UUID

@Entity
data class BugEventEntity(
        @PrimaryKey override var id: UUID = UUID.randomUUID(),
        override val createdAt: Instant = Instant.now(),
        override var tag: String? = null,
        override val info: String? = null,
        override val exceptionClass: String,
        override val exceptionMessage: String? = null,
        override val stackTrace: String,
        override val deviceInfo: String,
        override val appVersionName: String,
        override val appVersionCode: Int,
        override val apiLevel: Int,
        override val androidVersion: String,
        override val shortCommitHash: String,
        override val logHistory: List<String>
) : BugEvent
