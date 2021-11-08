/*
    Copyright 2021 A-SIT Plus GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    Modifications Copyright (c) 2021 SAP SE or an SAP affiliate company.
*/
package de.rki.coronawarnapp.util.encoding

import java.math.BigInteger

/**
 * Based on
 * https://github.com/corona-warn-app/cwa-app-android/blob/84fb3841ebdc01168a77ddff5570ae5ce678058d/Corona-Warn-App/src/main/java/de/rki/coronawarnapp/util/encoding/Base45Decoder.kt
 *
 */

private const val DIVISOR = 45
private const val ENCODING_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
private val decoding_charset = ByteArray(256) { -1 }.also { charset ->
    ENCODING_CHARSET.encodeToByteArray().forEachIndexed { index, byte ->
        charset[byte.toInt()] = index.toByte()
    }
}
private val int256 = BigInteger.valueOf(256)

object Base45Decoder {

    fun encode(input: ByteArray) =
        input.asSequence()
            .map { it.toUByte() }
            .chunked(2)
            .map(this::encodeTwoCharsPadded)
            .flatten()
            .joinToString(separator = "")

    private fun encodeTwoCharsPadded(input: List<UByte>): List<Char> {
        val result = encodeTwoChars(input).toMutableList()
        when (input.size) {
            1 -> if (result.size < 2) result += '0'
            2 -> while (result.size < 3) result += '0'
        }
        return result
    }

    private fun encodeTwoChars(list: List<UByte>) =
        generateSequenceByDivRem(toTwoCharValue(list))
            .map { ENCODING_CHARSET[it] }.toList()

    private fun toTwoCharValue(list: List<UByte>) =
        list.reversed().foldIndexed(0L) { index, acc, element ->
            pow(index) * element.toShort() + acc
        }

    private fun generateSequenceByDivRem(seed: Long) =
        generateSequence(seed) { if (it >= DIVISOR) it.div(DIVISOR) else null }
            .map { it.rem(DIVISOR).toInt() }

    private fun pow(exp: Int) = int256.pow(exp).toLong()

    @Throws(IllegalArgumentException::class)
    fun decode(input: String): ByteArray =
        input.toByteArray().asSequence().map {
            decoding_charset[it.toInt()].also { index ->
                if (index < 0) throw IllegalArgumentException("Invalid characters in input.")
            }
        }.chunked(3) { chunk ->
            if (chunk.size < 2) throw IllegalArgumentException("Invalid input length.")
            chunk.reversed().toInt(45).toBase(base = 256, count = chunk.size - 1).reversed()
        }.flatten().toList().toByteArray()

    /** Converts integer to a list of [count] integers in the given [base]. */
    @Throws(IllegalArgumentException::class)
    private fun Int.toBase(base: Int, count: Int): List<Byte> =
        mutableListOf<Byte>().apply {
            var tmp = this@toBase
            repeat(count) {
                add((tmp % base).toByte())
                tmp /= base
            }
            if (tmp != 0) throw IllegalArgumentException("Invalid character sequence.")
        }

    /** Converts list of bytes in given [base] to an integer. */
    private fun List<Byte>.toInt(base: Int): Int =
        fold(0) { acc, i -> acc * base + i.toUByte().toInt() }
}
