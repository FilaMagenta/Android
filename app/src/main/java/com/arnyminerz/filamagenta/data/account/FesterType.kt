package com.arnyminerz.filamagenta.data.account

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.arnyminerz.filamagenta.R

private const val DB_TYPE_ALE = 1
private const val DB_TYPE_INF = 2
private const val DB_TYPE_JUV = 3
private const val DB_TYPE_SIT = 4
private const val DB_TYPE_FES = 5
private const val DB_TYPE_JUB = 6
private const val DB_TYPE_COL = 7
private const val DB_TYPE_BAI = 8
private const val DB_TYPE_SIE = 9
private const val DB_TYPE_COP = 10
private const val DB_TYPE_JUP = 11
private const val DB_TYPE_OTH = -1

enum class FesterType(
    val dbType: Int,
    @StringRes val localizedName: Int,
    val color: Color,
) {
    ALEVIN(DB_TYPE_ALE, R.string.fester_type_alevin, Colors.White),
    INFANTIL(DB_TYPE_INF, R.string.fester_type_infantil, Colors.White),
    JUVENIL(DB_TYPE_JUV, R.string.fester_type_juvenil, Colors.White),
    SIT_ESP(DB_TYPE_SIT, R.string.fester_type_sit_esp, Colors.Blue),
    FESTER(DB_TYPE_FES, R.string.fester_type_fester, Colors.Orange),
    JUBILAT(DB_TYPE_JUB, R.string.fester_type_jubilat, Colors.Yellow),
    COLABORADOR(DB_TYPE_COL, R.string.fester_type_colaborador, Colors.Yellow),
    BAIXA(DB_TYPE_BAI, R.string.fester_type_baixa, Colors.Black),
    SIT_ESP_ESTUDIS(DB_TYPE_SIE, R.string.fester_type_sit_esp_estudis, Colors.Blue),
    COL_PACK(DB_TYPE_COP, R.string.fester_type_colaborador_pack, Colors.Yellow),
    JUBILAT_PACK(DB_TYPE_JUP, R.string.fester_type_jubilat_pack, Colors.Yellow),
    UNKNOWN(DB_TYPE_OTH, R.string.fester_type_unknown, Colors.Blue);

    companion object {
        fun valueOf(dbType: Int) = values().find { it.dbType == dbType } ?: UNKNOWN

        object Colors {
            // https://coolors.co/231f20-bb4430-7ebdc2-f3dfa2-efe6dd

            val Black = Color(0xFF231F20)
            val Orange = Color(0xFFBB4430)
            val Blue = Color(0xFF7EBDC2)
            val Yellow = Color(0xFFF3DFA2)
            val White = Color(0xFFEFE6DD)
        }
    }
}
