package com.arnyminerz.filamagenta.utils.serialize

interface DatabaseSerializer<R> {
    fun fromDatabaseRow(row: Map<String, Any?>): R
}
