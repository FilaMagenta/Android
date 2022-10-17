package com.arnyminerz.filamagenta.utils.serialize

fun <R, T: DatabaseSerializer<R>> List<Map<String, Any?>>.mapDatabaseEntries(serializer: T): List<R> =
    map { serializer.fromDatabaseRow(it) }
