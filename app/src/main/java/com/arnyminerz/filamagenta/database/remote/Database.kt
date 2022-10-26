package com.arnyminerz.filamagenta.database.remote

import androidx.annotation.WorkerThread
import net.sourceforge.jtds.jdbc.JtdsConnection
import timber.log.Timber
import java.sql.Date
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.reflect.KClass

@Deprecated("Use REST API with RemoteInterface.")
class Database(
    private val hostname: String,
    private val database: String,
    private val username: String,
    private val password: String,
) {
    private var conn: JtdsConnection? = null

    val isDisconnected: Boolean
        get() = conn == null || conn?.isClosed != false

    @Throws(SQLException::class)
    fun connect() {
        if (conn != null)
            conn?.close()

        // Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance()
        conn = DriverManager.getConnection(
            "jdbc:jtds:sqlserver://$hostname/$database;user=$username;password=$password;",
        ) as JtdsConnection
    }

    fun close() = conn?.close()

    /**
     * Runs a `SELECT`query to the database. Connects if not connected. Does not disconnect.
     * @author Arnau Mora
     * @since 20221014
     * @param table The table to select from.
     * @param fields A map containing the column names to obtain, and their respective types.
     * @param where A map containing some conditions to apply to the select query (as `WHERE`). The
     * keys match the column names, and the values their respective values. Values will be quoted
     * automatically, so no `NULL` or other types are allowed. If null, no condition will apply.
     * @return A list with all the obtained rows. Each element of the list matches a row in the
     * database, and the keys of the resulting map matches the keys given into [fields], and the
     * values are replaced with the data obtained from the db, as the type given in [fields].
     * @throws SQLException If there's an error while running the SQL query.
     * @throws UnsupportedOperationException When given a type through [fields] that cannot be
     * handled.
     * @see connect
     */
    @WorkerThread
    @Throws(SQLException::class, UnsupportedOperationException::class)
    fun select(
        table: String,
        fields: Map<String, KClass<*>>,
        where: Map<String, String>? = null,
    ): List<Map<String, Any?>> {
        if (isDisconnected)
            connect()
        val whereQuery = where
            ?.takeIf { it.isNotEmpty() }
            ?.map { (key, value) -> "$key='$value'" }
            ?.joinToString(" AND ")
            ?.let { " WHERE $it" }
            ?: ""

        val comm = conn!!.createStatement()
        val query = "SELECT ${fields.keys.joinToString(",")} FROM $table$whereQuery"
        Timber.v("SQL: $query")

        val rs = try {
            comm.executeQuery(query)
        } catch (e: SQLException) {
            Timber.e("Could not run query: $query")
            throw e
        }
        val builder = arrayListOf<Map<String, Any?>>()
        while (rs.next()) {
            val row = mutableMapOf<String, Any?>()
            for ((key, type) in fields)
                row[key] = when (type) {
                    String::class -> rs.getString(key)
                    Int::class -> rs.getInt(key)
                    Long::class -> rs.getLong(key)
                    Date::class -> rs.getDate(key)
                    Boolean::class -> rs.getBoolean(key)
                    else -> throw UnsupportedOperationException(
                        "The given class type ($type) for column $key is not a valid type for the database."
                    )
                }
            builder.add(row)
        }
        return builder
    }

    /**
     * Runs an `INSERT` query to the database.
     * @author Arnau Mora
     * @since 20221015
     * @param table The table to insert into.
     * @param values The values to set. The key matches the column in the table, and the value what
     * to set in the row. [String]s get quoted automatically, other types are passed with their
     * respective `toString` method.
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class)
    fun insert(table: String, values: Map<String, Any>): Boolean {
        if (isDisconnected)
            connect()

        val valuesStrings = values.toList().let { list ->
            val a = list.joinToString { it.first }
            val b = list.joinToString { (_, v) -> if (v is String) "'$v'" else v.toString() }
            a to b
        }

        val query = "INSERT INTO $table (${valuesStrings.first}) VALUES (${valuesStrings.second})"
        val comm = conn!!.createStatement()
        return try {
            comm.execute(query)
        } catch (e: SQLException) {
            Timber.e("Could not run query: $query")
            throw e
        }
    }

    fun update(
        table: String,
        values: Map<String, Any>,
        where: Map<String, String>? = null
    ): Boolean {
        if (values.isEmpty())
            return true

        if (isDisconnected)
            connect()
        val whereQuery = where
            ?.takeIf { it.isNotEmpty() }
            ?.map { (key, value) -> "$key='$value'" }
            ?.joinToString(" AND ")
            ?.let { " WHERE $it" }
            ?: ""

        val set = values
            .mapValues { (_, value) -> if (value is String) "'$value'" else value.toString() }
            .toList()
            .joinToString { (key, value) -> "$key=$value" }
        val query = "UPDATE $table SET $set$whereQuery;"
        val comm = conn!!.createStatement()
        return try {
            comm.execute(query)
        } catch (e: SQLException) {
            Timber.e("Could not run query: $query")
            throw e
        }
    }
}
