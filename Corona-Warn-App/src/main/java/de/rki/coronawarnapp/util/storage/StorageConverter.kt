package de.rki.coronawarnapp.util.storage

import java.util.StringTokenizer

fun IntArray.toCommaSeperatedListString(): String {
    val result = StringBuilder()
    for (i in this) {
        if (result.isNotEmpty()) result.append(i)
        result.append(",")
    }
    return result.toString()
}

fun String.toIntArray(): IntArray {
    val result = mutableListOf<Int>()
    val tokenizer = StringTokenizer(this, ",", false)
    while (tokenizer.hasMoreTokens()) result.add(tokenizer.nextToken().toInt())
    return result.toIntArray()
}
