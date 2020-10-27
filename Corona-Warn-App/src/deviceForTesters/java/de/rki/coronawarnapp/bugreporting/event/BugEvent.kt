package de.rki.coronawarnapp.bugreporting.event

import org.joda.time.Instant

interface BugEvent {
    val id: Long
    val createdAt: Instant
    val tag: String?
    val info: String?
    val exceptionClass: String
    val exceptionMessage: String?
    val stackTrace: String
    val appVersionName: String
    val appVersionCode: Int
    val deviceInfo: String
    val apiLevel: Int
    val androidVersion: String
    val shortCommitHash: String
    val logHistory: List<String>
}
