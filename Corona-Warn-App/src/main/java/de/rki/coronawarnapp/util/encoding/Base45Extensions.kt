package de.rki.coronawarnapp.util.encoding

private const val BASE: Int = 45
private const val BASE_SQUARED: Int = BASE * BASE
/** Maps the numbers 0..44 (indices) to the characters representing that number (char at that index) */
private const val ENCODING_CHARSET: String = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
/** Maps the characters from [ENCODING_CHARSET] to the corresponding index of that character */
private val decodingCharset: Map<Char, Int> = ENCODING_CHARSET.associateWith { ENCODING_CHARSET.indexOf(it) }

/**
 * Decodes [String] into [ByteArray] using Base45 decoder
 * @author floscher
 * @return [ByteArray]
 */
@Throws(IllegalArgumentException::class)
fun String.decodeBase45(): ByteArray = this
    // string length must be a multiple of three or one less
    .also { require (it.length % 3 != 1) { "Illegal length of Base45 string (${it.length})!" } }
    // convert characters to numbers in range 0..44
    .map { decodingCharset.getOrElse(it) { throw IllegalArgumentException("Illegal Base45 character ($it)!") } }
    .let { numberInput ->
        // an int progression containing the first index of each complete group of three numbers
        (0 until this.length - 2 step 3)
            // Calculate the value of each group of three as a three-digit number in base 45 (LE)
            .map { numberInput[it] + numberInput[it + 1] * BASE + numberInput[it + 2] * BASE_SQUARED }
            // Convert that number to two bytes (BE)
            .flatMap { listOf(it / 256, it % 256) }
            .let {
                // If there is a group of two numbers left over at the end, treat them as one two-digit number in base 45 (LE)
                if (this.length % 3 == 2) {
                    it.plus(numberInput[numberInput.size - 2] + numberInput[numberInput.size - 1] * BASE)
                } else it
            }
            // Convert to byte array
            .map { it.toUByte().toByte() }
            .toByteArray()
    }

/**
 * Encodes [ByteArray] into base45 [String]
 * @author floscher
 * @return [String]
 */
fun ByteArray.base45(): String =
    // an int progression containing the first index of each complete pair of bytes
    (0 until this.size - 1 step 2)
        .flatMap { i ->
            // calculate the value for each pair of bytes (BE)
            (this[i].toUByte().toInt() * 256 + this[i + 1].toUByte().toInt()).let {
                // convert the number to base 45 digits (LE), all values will later be converted to their `mod 45`
                listOf(it, it / BASE, it / BASE_SQUARED)
            }
        }
        .plus(
            // Take the last byte if there is one, …
            this.lastOrNull()
                // … and only if there is one single lonely byte at the end after all others are paired up
                ?.takeIf { this.size % 2 != 0 }
                ?.toUByte()
                ?.toInt()
                // convert that to a two-digit number in base 45 (LE), all values will later be converted to their `mod 45`
                ?.let { listOf(it, it / BASE) }
                ?: listOf()
        )
        // Convert the list to Base-45-"digits" using `mod 45`, then convert to the corresponding ASCII characters
        .map { ENCODING_CHARSET[it % BASE] }
        .toCharArray()
        .concatToString()
