package de.rki.coronawarnapp.nearby.modules.locationless

import kotlinx.coroutines.flow.Flow

interface ScanningSupport {
    val isLocationLessScanningSupported: Flow<Boolean>
}
