package de.rki.coronawarnapp.util.dcc

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

/**
The operation shall group set of DGCs irrespective of their type
(e.g. Vaccination Certificate, Test Certificate, or Recovery Certificate) by the same holder.
 */
fun Set<CwaCovidCertificate>.groupCertificatesByPerson(): List<Set<CwaCovidCertificate>> {
    var groups = map { setOf(it) }
    var newGroups: List<Set<CwaCovidCertificate>> = groups
    do {
        groups = newGroups
        newGroups = dgcGroupingIteration(groups)
    } while (newGroups != groups)
    return newGroups
}

/**
Method shall decide whether the DGCs belongs the same holder.
Two DCCs shall be considered as belonging to the same holder, if:

- the sanitized `dob` attributes are the same strings, and
- one of:
 - the intersection/overlap of the name components of sanitized `a.nam.fnt` and `b.nam.fnt` has at least one element, and
 the intersection/overlap of the name components of sanitized `a.nam.gnt` and `b.nam.gnt` has at least one element or both are empty sets (`gnt` is an optional field)
 - the intersection/overlap of the name components of sanitized `a.nam.fnt` and `b.nam.gnt` has at least one element, and
 the intersection/overlap of the name components of sanitized `a.nam.gnt` and `b.nam.fnt` has at least one element
 */
fun belongToSamePerson(certA: CwaCovidCertificate, certB: CwaCovidCertificate): Boolean {
    if (certA.dateOfBirthFormatted.trim() != certB.dateOfBirthFormatted.trim()) return false

    if (certA.sanitizedFamilyName.intersect(certB.sanitizedFamilyName).isNotEmpty()) {
        if (certA.sanitizedGivenName.intersect(certB.sanitizedGivenName)
            .isNotEmpty() || (certA.sanitizedGivenName.isEmpty() && certB.sanitizedGivenName.isEmpty())
        ) {
            return true
        }
    }

    if (certA.sanitizedFamilyName.intersect(certB.sanitizedGivenName).isNotEmpty() &&
        certA.sanitizedGivenName.intersect(certB.sanitizedFamilyName).isNotEmpty()
    ) {
        return true
    }

    return false
}

/**
 * One step in certificate grouping. This method needs to be called multiple times.
 * In every step method create more accurate results. Let's say you have certificates registered to
 * Dr. Thomas, Dr. Martin, and Thomas Martin. In the beginning, we don't know that Dr. Thomas and
 * Dr. Martin is the same person, but after 1st iteration, we know that we can group them together.
 */
private fun dgcGroupingIteration(input: List<Set<CwaCovidCertificate>>): List<Set<CwaCovidCertificate>> {
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

private fun List<Set<CwaCovidCertificate>>.removeDuplicates(): List<Set<CwaCovidCertificate>> {
    val output = mutableListOf<Set<CwaCovidCertificate>>()
    reversed().forEachIndexed { index, set ->
        var isDuplicate = false
        for (i in 0 until size - index - 1) {
            if (get(i).containsAll(set)) {
                isDuplicate = true
            }
        }
        if (!isDuplicate) {
            output.add(set)
        }
    }
    return output.reversed()
}

/**
 * @return [True] if two certificate sets share certificates belonging to the same person
 */
private fun Set<CwaCovidCertificate>.personIntersect(certificates: Set<CwaCovidCertificate>): Boolean {
    forEach { certificateA ->
        certificates.forEach { certificateB ->
            if (belongToSamePerson(certificateA, certificateB)) return true
        }
    }
    return false
}
