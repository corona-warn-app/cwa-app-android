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
 * https://github.com/ehn-digital-green-development/hcert-kotlin/blob/23203fbb71f53524ee643a9df116264f87b5b32a/src/main/kotlin/ehn/techiop/hcert/kotlin/chain/common/Base45Encoder.kt
 */
@OptIn(ExperimentalUnsignedTypes::class)
object Base45Decoder {
    private const val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
    private val int45 = BigInteger.valueOf(45)
    private val int256 = BigInteger.valueOf(256)

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
        generateSequenceByDivRem(toTwoCharValue(list), 45)
            .map { alphabet[it] }.toList()

    private fun toTwoCharValue(list: List<UByte>) =
        list.reversed().foldIndexed(0L) { index, acc, element ->
            pow(int256, index) * element.toShort() + acc
        }

    fun decode(input: String) =
        input.chunked(3).map(this::decodeThreeCharsPadded)
            .flatten().map { it.toByte() }.toByteArray()

    private fun decodeThreeCharsPadded(input: String): List<UByte> {
        val result = decodeThreeChars(input).toMutableList()
        when (input.length) {
            3 -> while (result.size < 2) result += 0U
        }
        return result.reversed()
    }

    private fun decodeThreeChars(list: String) =
        generateSequenceByDivRem(fromThreeCharValue(list), 256)
            .map { it.toUByte() }.toList()

    private fun fromThreeCharValue(list: String): Long {
        return list.foldIndexed(
            0L,
            { index, acc: Long, element ->
                if (!alphabet.contains(element))
                    throw IllegalArgumentException(element.toString())
                pow(int45, index) * alphabet.indexOf(element) + acc
            }
        )
    }

    private fun generateSequenceByDivRem(seed: Long, divisor: Int) =
        generateSequence(seed) { if (it >= divisor) it.div(divisor) else null }
            .map { it.rem(divisor).toInt() }

    private fun pow(base: BigInteger, exp: Int) = base.pow(exp).toLong()
}
