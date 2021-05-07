package de.rki.coronawarnapp.vaccination.decoder

import java.math.BigInteger
import javax.inject.Inject

// TODO:licence

@OptIn(ExperimentalUnsignedTypes::class)
class Base45Decoder @Inject constructor() {
    private val alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
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
