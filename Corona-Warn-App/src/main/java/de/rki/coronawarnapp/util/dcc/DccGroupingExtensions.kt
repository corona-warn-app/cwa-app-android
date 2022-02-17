package de.rki.coronawarnapp.util.dcc

import de.rki.coronawarnapp.covidcertificate.common.certificate.CertificatePersonIdentifier
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

/**
The operation shall group set of DGCs irrespective of their type
(e.g. Vaccination Certificate, Test Certificate, or Recovery Certificate) by the same holder.
 */
fun Set<CwaCovidCertificate>.groupByPerson(): List<Set<CwaCovidCertificate>> {
    var groups = map { setOf(it) }
    var newGroups: List<Set<CwaCovidCertificate>> = groups
    do {
        groups = newGroups
        newGroups = dgcGroupingIteration(groups)
    } while (newGroups != groups)
    return newGroups
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
 * @return True if two certificate sets share certificates belonging to the same person
 */
private fun Set<CwaCovidCertificate>.personIntersect(certificates: Set<CwaCovidCertificate>): Boolean {
    forEach { certificateA ->
        certificates.forEach { certificateB ->
            if (certificateA.personIdentifier.isTheSamePerson(certificateB)) return true
        }
    }
    return false
}

fun List<Set<CwaCovidCertificate>>.firstGroupWithPerson(personIdentifier: CertificatePersonIdentifier?): Set<CwaCovidCertificate> {
    if (personIdentifier == null) return emptySet()
    forEach { certificates ->
        certificates.forEach { certificate ->
            if (personIdentifier.isTheSamePerson(certificate)) {
                return certificates
            }
        }
    }
    return emptySet()
}
