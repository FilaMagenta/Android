package com.arnyminerz.filamagenta.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import com.arnyminerz.filamagenta.utils.doAsync
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class Authenticator(context: Context) : AbstractAccountAuthenticator(context) {
    private val accountSingleton = AccountSingleton.getInstance(context)

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle? = null

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        options: Bundle?
    ): Bundle? = null

    override fun updateCredentials(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        throw UnsupportedOperationException()
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse?,
        account: Account?,
        features: Array<out String>?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun getAccountRemovalAllowed(
        response: AccountAuthenticatorResponse?,
        account: Account?
    ): Bundle {
        val result = super.getAccountRemovalAllowed(response, account)
        if (result.containsKey(AccountManager.KEY_BOOLEAN_RESULT) && !result.containsKey(AccountManager.KEY_INTENT)) {
            val removalAllowed = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)
            if (removalAllowed && account != null) {
                Timber.w("Removing account ${account.name}...")
                accountSingleton.notifyAccountRemoved(account)
                Timber.w("${account.name} removed!")
            }
        }
        return result
    }
}
