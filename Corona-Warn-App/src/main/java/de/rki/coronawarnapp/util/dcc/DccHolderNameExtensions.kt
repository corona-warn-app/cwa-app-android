package de.rki.coronawarnapp.util.dcc

val filteringList = listOf("DR")

fun String.cleanHolderName(): List<String> {
    return uppercase()
        .trim()
        .trim('<')
        .replace("\\s+".toRegex(), "<")
        .replace(".", "<")
        .replace("-", "<")
        .replace("Ä", "AE")
        .replace("Ö", "OE")
        .replace("Ü", "UE")
        .replace("ß", "SS")
        .replace("<+".toRegex(), "<")
        .split("<")
        .filter { !filteringList.contains(it) }
        .filter { it.isNotBlank() }
}
