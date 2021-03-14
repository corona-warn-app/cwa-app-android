package de.rki.coronawarnapp.test.risklevel.entities

import com.google.android.gms.nearby.exposurenotification.ScanInstance

data class ScanInstanceJson(
    val typicalAttenuationDb: Int,
    val minAttenuationDb: Int,
    val secondsSinceLastScan: Int
)

fun ScanInstance.toScanInstanceJson(): ScanInstanceJson = ScanInstanceJson(
    typicalAttenuationDb = typicalAttenuationDb,
    minAttenuationDb = minAttenuationDb,
    secondsSinceLastScan = secondsSinceLastScan
)
