package com.arnyminerz.filamagenta.data.account

import androidx.annotation.StringRes
import com.arnyminerz.filamagenta.R

private const val DB_TYPE_TRA = 1
private const val DB_TYPE_REM = 2
private const val DB_TYPE_CAJ = 3
private const val DB_TYPE_TPV = 4
private const val DB_TYPE_WTP = 5
private const val DB_TYPE_OTH = 6

enum class PaymentMethod(val id: Int, @StringRes val localizedName: Int) {
    TRANSFERENCIA(DB_TYPE_TRA, R.string.payment_method_transferencia),
    REMESA(DB_TYPE_REM, R.string.payment_method_remesa),
    CAJA(DB_TYPE_CAJ, R.string.payment_method_caja),
    DATAFONO(DB_TYPE_TPV, R.string.payment_method_tpv),
    TPV_WEB(DB_TYPE_WTP, R.string.payment_method_tpv_web),
    UNKNOWN(DB_TYPE_OTH, R.string.payment_method_unknown);

    companion object {
        fun valueOf(dbType: Int) = values().find { it.id == dbType } ?: UNKNOWN
    }
}
