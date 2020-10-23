package de.rki.coronawarnapp.bugreporting.event

import org.joda.time.Instant

class DefaultBugEvent(
    override val id: Long,
    override val createdAt: Instant,
    override val tag: String?,
    override val info: String?,
    override val exceptionClass: String,
    override val exceptionMessage: String?,
    override val stackTrace: String,
    override val deviceInfo: String,
    override val appVersionName: String,
    override val appVersionCode: Int,
    override val apiLevel: Int,
    override val androidVersion: String,
    override val shortID: String,
    override val logHistory: List<String>
) : BugEvent
