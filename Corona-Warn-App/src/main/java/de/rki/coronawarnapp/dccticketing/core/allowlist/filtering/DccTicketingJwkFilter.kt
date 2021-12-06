package de.rki.coronawarnapp.dccticketing.core.allowlist.filtering

import de.rki.coronawarnapp.dccticketing.core.allowlist.data.DccTicketingValidationServiceAllowListEntry
import de.rki.coronawarnapp.dccticketing.core.check.createSha256Fingerprint
import de.rki.coronawarnapp.dccticketing.core.common.DccJWKConverter
import de.rki.coronawarnapp.dccticketing.core.transaction.DccJWK
import de.rki.coronawarnapp.tag
import timber.log.Timber
import javax.inject.Inject

class DccTicketingJwkFilter @Inject constructor(
    private val dccJWKConverter: DccJWKConverter,
) {

    fun filter(
        jwkSet: Set<DccJWK>,
        validationServiceAllowList: Set<DccTicketingValidationServiceAllowListEntry>
    ): DccJwkFilteringResult {
        Timber.tag(TAG).d("filter()")

        val allowListFingerprints = validationServiceAllowList.map { it.fingerprint256 }
        val jwkFingerprintsMap = jwkSet.associateBy {
            dccJWKConverter.createX509Certificate(jwk = it).createSha256Fingerprint()
        }
        val fingerprintIntersection = allowListFingerprints intersect jwkFingerprintsMap.keys.also {
            Timber.tag(TAG).d("fingerprintIntersection=%s", it)
        }

        val filteredAllowlist = validationServiceAllowList.filter {
            fingerprintIntersection.contains(it.fingerprint256)
        }.toSet()

        val filteredJwkSet = jwkFingerprintsMap
            .filter { entry -> fingerprintIntersection.contains(entry.key) }
            .values
            .toSet()

        return DccJwkFilteringResult(
            filteredAllowlist = filteredAllowlist,
            filteredJwkSet = filteredJwkSet
        ).also { Timber.tag(TAG).d("DccJwkFilteringResult=%s", it) }
    }

    companion object {
        private val TAG = tag<DccTicketingJwkFilter>()
    }
}
