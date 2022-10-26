package com.arnyminerz.filamagenta.utils.serialize

@Deprecated("Database is no longer used.", replaceWith = ReplaceWith("JsonSerializer<R>"))
interface DatabaseSerializer<R> {
    fun fromDatabaseRow(row: Map<String, Any?>): R
}
