package de.rki.coronawarnapp.util.dcc

import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate

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

    if (certificateAFirstNames.intersect(certificateBLastNames).isNotEmpty() &&
        certificateALastNames.intersect(certificateBFirstNames).isNotEmpty()
    ) {
        return true
    }

    return false
}

fun Set<CwaCovidCertificate>.group(): List<Set<CwaCovidCertificate>> {
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
