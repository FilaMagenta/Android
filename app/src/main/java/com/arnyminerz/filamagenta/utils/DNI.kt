package com.arnyminerz.filamagenta.utils

import androidx.annotation.Size

private val DNIFormatRegex = Regex("^\\d{8}[TRWAGMYFPDXBNJZSQVHLCKE]$")

private const val DNILetters = "TRWAGMYFPDXBNJZSQVHLCKE"

/**
 * Obtains the letter that corresponds to the given DNI.
 * @author Arnau Mora
 * @since 20221011
 * @param dni The DNI with letter included.
 * @return The letter that corresponds to the given DNI.
 */
@Throws(NumberFormatException::class)
fun letterDNI(@Size(9L) dni: String): Char {
    val num = dni.substring(0, 8).toInt()
    val mod = num % 23
    return DNILetters[mod]
}

/**
 * Checks if the string is a valid DNI, checking the number and the letter. Must have 9 characters.
 * @author Arnau Mora
 * @since 20221011
 * @see letterDNI
 */
val String.isValidDNI: Boolean
    get() = if (length != 9 || !get(8).isLetter() || !matches(DNIFormatRegex))
        false
    else try {
        letterDNI(this).let { last().equals(it, true) }
    } catch (e: NumberFormatException) { false }
