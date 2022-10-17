package com.arnyminerz.filamagenta.data.event

import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.utils.asStringList
import com.arnyminerz.filamagenta.utils.entries
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONArray
import org.json.JSONObject

data class Menu(
    val price: Map<FesterType, Double>,
    val starters: List<String>,
    val firsts: List<String>,
    val seconds: List<String>,
    val dessert: List<String>,
    val drink: Boolean,
    val coffee: Boolean,
): JsonSerializable() {
    companion object: JsonSerializer<Menu> {
        override fun fromJson(json: JSONObject): Menu = Menu(
            json.getJSONObject("price")
                .entries
                .mapKeys { (key, _) -> FesterType.valueOf(key) }
                .mapValues { (_, value) -> if (value is Int) value.toDouble() else value as Double },
            json.getJSONArray("starters").asStringList,
            json.getJSONArray("firsts").asStringList,
            json.getJSONArray("seconds").asStringList,
            json.getJSONArray("dessert").asStringList,
            json.getBoolean("drink"),
            json.getBoolean("coffee"),
        )
    }

    val rounds = listOf(starters, firsts, seconds, dessert)

    override fun toJson(): JSONObject = JSONObject().apply {
        put("price", JSONObject(price.mapKeys { it.key.name }))
        put("starters", JSONArray(starters))
        put("firsts", JSONArray(firsts))
        put("seconds", JSONArray(seconds))
        put("dessert", JSONArray(dessert))
        put("drink", drink)
        put("coffee", coffee)
    }
}
