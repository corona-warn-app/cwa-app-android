package de.rki.coronawarnapp.util.dcc

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

fun Set<CwaCovidCertificate>.group(): Map<DccHolderGroupKey, List<CwaCovidCertificate>> {
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
        if (key.firstName != null) (firstName.plus("<").plus(key.firstName)).split("<").distinct().sortedBy { it }
            .joinToString("<")
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
    var lastNameMatch = lastNameA.intersect(lastNameB).joinToString(separator = "<")

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

fun isItSamePerson(certificateA: CwaCovidCertificate, certificateB: CwaCovidCertificate): Boolean {
    // TODO: don't use firstName, lastName
    if (certificateA.dateOfBirthFormatted.trim() != certificateB.dateOfBirthFormatted.trim()) return false
    val certificateAFirstNames = certificateA.firstName?.cleanHolderName() ?: emptyList()
    val certificateBFirstNames = certificateB.firstName?.cleanHolderName() ?: emptyList()
    val certificateALastNames = certificateA.lastName.cleanHolderName()
    val certificateBLastNames = certificateB.lastName.cleanHolderName()

    if (certificateALastNames.intersect(certificateBLastNames).isNotEmpty()) {
        if (certificateAFirstNames.intersect(certificateBFirstNames)
                .isNotEmpty() || (certificateAFirstNames.isEmpty() && certificateBFirstNames.isEmpty())
        ) {
            return true
        }
    }

    if (certificateAFirstNames.intersect(certificateBLastNames).isNotEmpty()
        && certificateALastNames.intersect(certificateBFirstNames).isNotEmpty()
    ) {
        return true
    }

    return false
}

fun Set<CwaCovidCertificate>.group2(): List<Set<CwaCovidCertificate>> {
    var groups = map { setOf(it) }
    var newGroups: List<Set<CwaCovidCertificate>> = groups
    do {
        groups = newGroups
        newGroups = mergeSamePersons(groups)
    } while (newGroups != groups)
    return newGroups
}

fun mergeSamePersons(input: List<Set<CwaCovidCertificate>>): List<Set<CwaCovidCertificate>> {
    val output = mutableListOf<Set<CwaCovidCertificate>>()
    input.forEachIndexed { index, set ->
        var newGroup = set
        for (i in index + 1 until input.size) {
            if (newGroup.personIntersect(input[i])) {
                newGroup = newGroup + input[i]
            }
        }
        output.add(newGroup)
    }
    return output.removeDuplicates()
}

fun List<Set<CwaCovidCertificate>>.removeDuplicates(): List<Set<CwaCovidCertificate>> {
    val output = mutableListOf<Set<CwaCovidCertificate>>()
    reversed().forEachIndexed { index, set ->
        var isAlreadyThere = false
        for (i in 0 until size - index - 1) {
            if (get(i).containsAll(set)) {
                isAlreadyThere = true
            }
        }
        if (!isAlreadyThere) {
            output.add(set)
        }
    }
    return output.reversed()
}

fun Set<CwaCovidCertificate>.personIntersect(certificates: Set<CwaCovidCertificate>): Boolean {
    forEach { certificateA ->
        certificates.forEach { certificateB ->
            if (isItSamePerson(certificateA, certificateB)) return true
        }
    }
    return false
}

fun Set<CwaCovidCertificate>.getSamePersons(cert: CwaCovidCertificate): Set<CwaCovidCertificate> {
    val set = mutableSetOf<CwaCovidCertificate>()
    forEach {
        if (isItSamePerson(cert, it)) {
            set.add(it)
        }
    }
    return set
}
