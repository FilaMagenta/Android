package com.arnyminerz.filamagenta.utils

/**
 * Throws [exception] if [predicate] returns false.
 * @author Arnau Mora
 * @since 20221025
 * @param predicate The condition for throwing [exception].
 * @param exception What to throw as error.
 * @return `this` if [predicate] returns `true`.
 */
fun <T : Any, E : Throwable> T.throwUnless(predicate: (obj: T) -> Boolean, exception: E): T =
    if (!predicate(this))
        throw exception
    else
        this
