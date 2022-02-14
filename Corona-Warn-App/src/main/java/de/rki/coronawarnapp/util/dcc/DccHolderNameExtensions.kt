package de.rki.coronawarnapp.util.dcc

private val filteringList = listOf("DR")
/*
Function sanitize names according to the following requirements:

- dots `.` and dashes `-` shall be replaced by `<`
- the string shall be converted to upper-case
- German umlauts `Ä/ä`, `Ö/ö`, `Ü/ü` shall be replaced by `AE`, `OE`, `UE`
- German `ß` shall be replaced by `SS`
- the string shall be trimmed for leading and training whitespace
- the string shall be trimmed for leading and trailing `<`
- any whitespace in the string shall be replaced by `<`
- any occurrence of more than one `<` shall be replaced by a single `<`

Name fields shall be split into components as follows:

- the string shall be split by `<`
- components with the value `DR` shall be filtered out
 */
fun String.sanitizeName(): List<String> {
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
