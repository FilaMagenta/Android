@file:Suppress("unused")

package com.arnyminerz.filamagenta.database.local

import androidx.room.TypeConverter
import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.data.account.PaymentMethod
import com.arnyminerz.filamagenta.data.event.Menu
import com.arnyminerz.filamagenta.data.event.TableData
import com.arnyminerz.filamagenta.utils.asLongList
import com.arnyminerz.filamagenta.utils.json
import com.arnyminerz.filamagenta.utils.serialize
import org.json.JSONArray
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun menuToString(menu: Menu?): String? {
        return menu?.toJson()?.toString()
    }

    @TypeConverter
    fun stringToMenu(menu: String?): Menu? {
        return menu?.json?.let { Menu.fromJson(it) }
    }

    @TypeConverter
    fun tableToString(table: TableData?): String? {
        return table?.toJson()?.toString()
    }

    @TypeConverter
    fun stringToTable(table: String?): TableData? {
        return table?.json?.let { TableData.fromJson(it) }
    }

    @TypeConverter
    fun tableListToString(list: List<TableData>?): String? =
        list?.let { l -> JSONArray(l.map { it.toJson() }).toString() }

    @TypeConverter
    fun stringToTableList(list: String?): List<TableData>? =
        list?.let { JSONArray(it).serialize(TableData.Companion) }

    @TypeConverter
    fun festerTypeToString(festerType: FesterType?): String? {
        return festerType?.name
    }

    @TypeConverter
    fun stringToFesterType(value: String?): FesterType? = value?.let { FesterType.valueOf(it) }

    @TypeConverter
    fun paymentMethodToString(value: PaymentMethod?): String? {
        return value?.name
    }

    @TypeConverter
    fun stringToPM(value: String?): PaymentMethod? = value?.let { PaymentMethod.valueOf(it) }

    @TypeConverter
    fun longListToString(value: List<Long>?): String? = value?.let { JSONArray(it).toString() }
    @TypeConverter
    fun stringToLongList(value: String?): List<Long>? = value?.let { JSONArray(it).asLongList }
}
