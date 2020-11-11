package de.rki.coronawarnapp.diagnosiskeys.download

import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKey
import de.rki.coronawarnapp.diagnosiskeys.storage.CachedKeyInfo
import de.rki.coronawarnapp.diagnosiskeys.storage.KeyCacheRepository
import de.rki.coronawarnapp.storage.DeviceStorage
import timber.log.Timber

open class BaseKeyPackageSyncTool(
    private val keyCache: KeyCacheRepository,
    private val deviceStorage: DeviceStorage,
    private val tag: String
) {

    /**
     * Returns true if any of our cached keys were revoked
     */
    internal suspend fun revokeCachedKeys(
        revokedKeyPackages: Collection<KeyDownloadConfig.RevokedKeyPackage>
    ): Boolean {
        if (revokedKeyPackages.isEmpty()) {
            Timber.tag(tag).d("No revoked key packages to delete.")
            return false
        }

        val badEtags = revokedKeyPackages.map { it.etag }
        val toDelete = keyCache.getAllCachedKeys().filter { badEtags.contains(it.info.etag) }

        return if (toDelete.isEmpty()) {
            Timber.tag(tag).d("No local cached keys matched the revoked ones.")
            false
        } else {
            Timber.tag(tag).w("Deleting revoked cached keys: %s", toDelete.joinToString("\n"))
            keyCache.delete(toDelete.map { it.info })
            true
        }
    }

    internal suspend fun requireStorageSpace(data: List<LocationData>): DeviceStorage.CheckResult {
        val requiredBytes = data.fold(0L) { acc, item ->
            acc + item.approximateSizeInBytes
        }
        Timber.tag(tag).d("%dB are required for %s", requiredBytes, data)
        return deviceStorage.requireSpacePrivateStorage(requiredBytes).also {
            Timber.tag(tag).d("Storage check result: %s", it)
        }
    }

    // All cached files that are no longer on the server are considered stale
    internal fun List<CachedKey>.findStaleData(
        availableData: List<LocationData>
    ): List<CachedKey> = filter { (cachedKey, _) ->
        // Is there a day on the server that matches our cached keys day?
        val serverHasMatchingDay = availableData
            .mapNotNull { it as? LocationDays }
            .any { it.dayData.contains(cachedKey.day) }

        when {
            cachedKey.type == CachedKeyInfo.Type.LOCATION_DAY -> {
                // If there is no matching day on the server, our cached key is stale
                return@filter !serverHasMatchingDay
            }
            cachedKey.type == CachedKeyInfo.Type.LOCATION_HOUR && serverHasMatchingDay -> {
                // A cached hour for which a server day exists, means we don't need the hour anymore
                // If there is no match, then we can't decide yet, and need to check the server for hours
                return@filter true // Stale
            }
        }

        // Is there an hour on the server that matches our cached hour?
        val serverHasMatchingHour = availableData
            .mapNotNull { it as? LocationHours }
            .any { serverHours ->
                serverHours.hourData.any { (day, hours) ->
                    cachedKey.day == day && hours.contains(cachedKey.hour)
                }
            }

        if (serverHasMatchingHour) {
            // Our hour is still on the server
            return@filter false // Not stale
        }

        // If we couldn't find match against the server data, our cache entry is probably stale
        return@filter true
    }

    internal suspend fun getDownloadedCachedKeys(
        location: LocationCode,
        type: CachedKeyInfo.Type
    ): List<CachedKey> = keyCache.getEntriesForType(type)
        .filter { it.info.location == location }
        .filter { key ->
            val complete = key.info.isDownloadComplete
            val exists = key.path.exists()
            if (complete && !exists) {
                Timber.tag(tag).v("Incomplete download, will overwrite: %s", key)
            }
            // We overwrite not completed ones
            complete && exists
        }

    data class SyncResult(
        val successful: Boolean = true,
        val newPackages: List<CachedKey> = emptyList()
    )
}
