package de.rki.coronawarnapp.nearby.modules.version

class OutdatedENFVersionException(
    val current: Long,
    val required: Long
) : Exception("Client is using an outdated ENF version: current=$current, required=$required")
