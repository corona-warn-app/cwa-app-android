package de.rki.coronawarnapp.util.dcc

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

fun Set<CwaCovidCertificate>.group(): Map<DccHolderGroupKey, List<CwaCovidCertificate>> {

//    val result = Map<CertificatePersonIdentifier, List<CwaCovidCertificate>>()
//    val result: Set<DccHolderGroupKey> = map { cwaCovidCertificate -> cwaCovidCertificate.toDccHolderGroupKey() }.toSet()
//    zipWithNext { a: CwaCovidCertificate, b: CwaCovidCertificate ->
//        val key = compare(a,b)
//        if(key != null) {
//
//            if(result.containsKey(key)) {
//                // If we already have the key in map - just add certificates and remove duplicates
//                result[key] = (listOf(a,b) + result[key]!!).distinct()
//            } else {
//                // If not : first check if we have a and b somewhere in map
//                listOf(a, b).forEach { cert ->
//                    var contained = false
//                    for (group in result.values) {
//                        if (group.contains(cert)) {
//                            contained = true
//                        }
//                    }
//                }
//                result[key] = listOf(a, b)
//            }
//        }
//    }
//                for (k in result.keys) {
//                    val mergedKey = compare(key, k)
// //                    if (mergedKey != null) {
// //
// //                    }
//                }
//

    val result = mutableMapOf<DccHolderGroupKey, List<CwaCovidCertificate>>()
    // First split on initial groups
    // At this point we could have multiple groups with same certificates
    forEach { certificateA ->
        for (certificateB in this) {
            if (certificateA == certificateB) {
                // Check if certificate exists in result
                var contained = false
                for (group in result.values) {
                    if (group.contains(certificateA)) {
                        contained = true
                    }
                }
                // If not contained anywhere - add it to self-group
                if (!contained) result[certificateA.toDccHolderGroupKey()] = listOf(certificateA)
            } else {
                val key = compare(certificateA, certificateB) ?: continue
                if (result.containsKey(key)) {
                    // If we already have the key in map - just add certificates and remove duplicates
                    result[key] = (listOf(certificateA, certificateB) + result[key]!!).distinct()
                } else {
                    // If not - add new key-value
                    result[key] = listOf(certificateA, certificateB)
                }
            }
        }
    }
    // Next we need to compose groups
    for (certificate in this) {
        val filteredResult = result.filter { entry -> entry.value.contains(certificate) }

        // If we have only one entry - no need to continue
        if (filteredResult.count() == 1) continue

        // Remove filtered from result
        result.minusAssign(filteredResult.keys)
        val certificates = filteredResult.values.flatten()
        var superKey = certificate.toDccHolderGroupKey()

        // Iterate and combine super key with all certificates we have filtered
        for (cert in certificates) {
            if (certificate == cert) continue
            superKey = superKey.combine(cert.toDccHolderGroupKey())
        }
        result[superKey] = certificates.distinct()
    }

    return result.toMap()
}

private fun DccHolderGroupKey.combine(key: DccHolderGroupKey): DccHolderGroupKey {
    val combinedFirstName = if (firstName != null) {
        if (key.firstName != null) (firstName.plus("<").plus(key.firstName)).split("<").distinct().sortedBy { it }.joinToString("<")
        else firstName
    } else key.firstName

    return DccHolderGroupKey(
        dateOfBirth = dateOfBirth.trim(),
        firstName = combinedFirstName,
        lastName = (lastName.plus("<").plus(key.lastName)).split("<").distinct().sortedBy { it }.joinToString("<")
    )
}

private fun compare(certificateA: CwaCovidCertificate, certificateB: CwaCovidCertificate): DccHolderGroupKey? {
    if (certificateA.dateOfBirthFormatted.trim() != certificateB.dateOfBirthFormatted.trim()) return null

    val firstNameA = certificateA.firstName?.cleanHolderName()
    val firstNameB = certificateB.firstName?.cleanHolderName()
    var firstNameMatch = if (firstNameA != null && firstNameB != null) {
        firstNameA.intersect(firstNameB).joinToString(separator = "<")
    } else return null

    val lastNameA = certificateA.lastName.cleanHolderName()
    val lastNameB = certificateB.lastName.cleanHolderName()
    var lastNameMatch = firstNameA.intersect(firstNameB).joinToString(separator = "<")

    if (firstNameMatch.isNotEmpty() && lastNameMatch.isNotEmpty()) {
        return DccHolderGroupKey(
            dateOfBirth = certificateA.dateOfBirthFormatted.trim(),
            firstName = (firstNameA + firstNameB).distinct().joinToString(separator = "<"),
            lastName = (lastNameA + lastNameB).distinct().joinToString(separator = "<")
        )
    }

    firstNameMatch = firstNameA.intersect(lastNameB).joinToString(separator = "<")
    lastNameMatch = firstNameB.intersect(lastNameA).joinToString(separator = "<")

    if (firstNameMatch.isNotEmpty() && lastNameMatch.isNotEmpty()) {
        return DccHolderGroupKey(
            dateOfBirth = certificateA.dateOfBirthFormatted.trim(),
            firstName = (firstNameA + lastNameB).distinct().joinToString(separator = "<"),
            lastName = (lastNameA + firstNameB).distinct().joinToString(separator = "<")
        )
    }

    return null
}

fun CwaCovidCertificate.toDccHolderGroupKey() = DccHolderGroupKey(
    dateOfBirth = dateOfBirthFormatted.trim(),
    firstName = firstName?.cleanHolderName()?.joinToString(separator = "<"),
    lastName = lastName.cleanHolderName().joinToString(separator = "<")
)

data class DccHolderGroupKey(
    val dateOfBirth: String,
    val firstName: String?,
    val lastName: String
) {
    val key = "$dateOfBirth#$lastName#$firstName"
}
