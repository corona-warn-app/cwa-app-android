package de.rki.coronawarnapp.statistics.local.storage

data class SelectedLocations(
    val locations: Set<SelectedStatisticsLocation> = emptySet()
) {
    fun withLocation(location: SelectedStatisticsLocation) =
        SelectedLocations(locations + location)

    fun withoutLocation(location: SelectedStatisticsLocation) =
        SelectedLocations(
            locations
                .filter { it.uniqueID != location.uniqueID }
                .toSet()
        )
}
