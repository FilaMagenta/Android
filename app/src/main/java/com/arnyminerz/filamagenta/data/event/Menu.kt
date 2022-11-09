package com.arnyminerz.filamagenta.data.event

import com.arnyminerz.filamagenta.data.account.FesterType
import com.arnyminerz.filamagenta.utils.asStringList
import com.arnyminerz.filamagenta.utils.getJSONArrayOrNull
import com.arnyminerz.filamagenta.utils.getStringOrNull
import com.arnyminerz.filamagenta.utils.objects
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializable
import com.arnyminerz.filamagenta.utils.serialize.JsonSerializer
import org.json.JSONArray
import org.json.JSONObject

data class Menu(
    val pricing: Map<FesterType, Double>,
    val starters: List<String>,
    val firsts: List<String>,
    val seconds: List<String>,
    val dessert: List<String>,
    val drink: Boolean,
    val coffee: Boolean,
    val tea: Boolean,
) : JsonSerializable() {
    companion object : JsonSerializer<Menu> {
        override fun fromJson(json: JSONObject): Menu = Menu(
            json.getJSONArray("pricing")
                .objects
                .associate { obj ->
                    val grade = obj.getStringOrNull("grade")
                        ?.takeIf { it != "null" }
                        .let { FesterType.valueOf(it) }
                    val price = obj.getDouble("price")
                    grade to price
                },
            json.getJSONArrayOrNull("starters")?.asStringList ?: emptyList(),
            json.getJSONArrayOrNull("firsts")?.asStringList ?: emptyList(),
            json.getJSONArrayOrNull("seconds")?.asStringList ?: emptyList(),
            json.getJSONArrayOrNull("dessert")?.asStringList ?: emptyList(),
            json.getBoolean("drinkIncluded"),
            json.getBoolean("coffeeIncluded"),
            json.getBoolean("teaIncluded"),
        )
    }

    val rounds = listOf(starters, firsts, seconds, dessert)

    override fun toJson(): JSONObject = JSONObject().apply {
        put(
            "pricing",
            JSONArray(pricing.map { (grade, price) ->
                JSONObject().apply {
                    put("grade", grade.dbType)
                    put("price", price)
                }
            })
        )
        put("starters", JSONArray(starters))
        put("firsts", JSONArray(firsts))
        put("seconds", JSONArray(seconds))
        put("dessert", JSONArray(dessert))
        put("drinkIncluded", drink)
        put("coffeeIncluded", coffee)
        put("teaIncluded", tea)
    }
}
