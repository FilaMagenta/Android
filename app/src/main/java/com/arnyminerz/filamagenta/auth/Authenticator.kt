package com.arnyminerz.filamagenta.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager.*
import android.content.Context
import android.os.Bundle
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.database.remote.RemoteInterface
import com.arnyminerz.filamagenta.utils.intent
import com.arnyminerz.filamagenta.utils.runBlocking
import timber.log.Timber

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
    private val accountSingleton = AccountSingleton.getInstance(context)

    private val remote = RemoteInterface.getInstance(context)

    override fun editProperties(
        response: AccountAuthenticatorResponse?,
        accountType: String?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val reply = Bundle()

        val intent = MainActivity::class.intent(context) {
            putExtra(MainActivity.EXTRA_ACCOUNT_TYPE, accountType)
            putExtra(MainActivity.EXTRA_AUTH_TOKEN_TYPE, authTokenType)
            putExtra(MainActivity.EXTRA_ADDING_NEW_ACCOUNT, true)
        }
        reply.putParcelable(KEY_INTENT, intent)

        return reply
    }

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
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle?
    ): Bundle {
        val accountManager = get(context)

        var token = accountManager.peekAuthToken(account, authTokenType)

        // Try authenticating the user
        if (token.isBlank())
            accountManager.getPassword(account)?.runBlocking { password ->
                token = remote.logIn(account.name, password)
            }

        // If the user is authenticated, return result
        if (token.isNotBlank())
            return Bundle().apply {
                putString(KEY_ACCOUNT_NAME, account.name)
                putString(KEY_ACCOUNT_TYPE, account.type)
                putString(KEY_AUTHTOKEN, token)
            }

        // If the user is not authenticated, an intent to login should be returned
        val intent = MainActivity::class.intent(context).apply {
            putExtra(KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(MainActivity.EXTRA_ACCOUNT_TYPE, account.type)
            putExtra(MainActivity.EXTRA_AUTH_TOKEN_TYPE, authTokenType)
            putExtra(KEY_ACCOUNT_NAME, account.name)
        }

        return Bundle().apply {
            putParcelable(KEY_INTENT, intent)
        }
    }

    override fun getAuthTokenLabel(authTokenType: String?): String =
        context.getString(R.string.account_type_name)

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
        if (result.containsKey(KEY_BOOLEAN_RESULT) && !result.containsKey(KEY_INTENT)) {
            val removalAllowed = result.getBoolean(KEY_BOOLEAN_RESULT)
            if (removalAllowed && account != null) {
                Timber.w("Removing account ${account.name}...")
                accountSingleton.notifyAccountRemoved(account)
                Timber.w("${account.name} removed!")
            }
        }
        return result
    }
}
