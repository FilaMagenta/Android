package com.arnyminerz.filamagenta.auth

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManager.KEY_AUTHTOKEN
import android.accounts.AccountManager.KEY_BOOLEAN_RESULT
import android.accounts.OperationCanceledException
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Handler
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filamagenta.BuildConfig
import com.arnyminerz.filamagenta.BuildConfig.ACCOUNT_TYPE
import com.arnyminerz.filamagenta.database.local.AppDatabase
import com.arnyminerz.filamagenta.database.local.entity.PersonData
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Used for managing the state of the authenticated user.
 * @author Arnau Mora
 * @since 20221011
 */
class AccountSingleton private constructor(context: Context) {
    companion object {
        @Volatile
        private var INSTANCE: AccountSingleton? = null

        /**
         * Gets the current instance of [AccountSingleton], or creates a new one if any available.
         * @author Arnau Mora
         * @since 20221011
         * @param context The context that is requesting the Singleton.
         * @return An instance of [AccountSingleton]
         */
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AccountSingleton(context).also {
                    INSTANCE = it
                }
            }
    }

    private val accountManager = AccountManager.get(context)

    private val database = AppDatabase.getInstance(context)

    /**
     * Observes the current log in status.
     * @author Arnau Mora
     * @since 20221011
     */
    val loggedIn = MutableLiveData<Boolean?>(null)

    private val accountsListState: MutableLiveData<List<Account>> = MutableLiveData()

    val accountsList: LiveData<List<Account>>
        get() = accountsListState

    init {
        accountManager.addOnAccountsUpdatedListener(
            { accountsListState.postValue(it.toList()) },
            Handler(context.mainLooper),
            true,
        )
    }

    /**
     * Gets the currently added accounts for the app.
     *
     * Updates [loggedIn] and posts into [accountsList].
     * @author Arnau mora
     * @since 20221011
     * @return A list of the added accounts.
     * @see addAccount
     */
    @WorkerThread
    fun getAccounts(): Array<Account> = accountManager.getAccountsByType(ACCOUNT_TYPE)
        .also { accountsListState.postValue(it.toList()) }
        .also { loggedIn.postValue(it.isNotEmpty()) }

    @WorkerThread
    fun notifyAccountRemoved(account: Account) = (accountsListState.value ?: emptyList())
        .toMutableList()
        .apply { remove(account) }
        .also { Timber.i("Removed account ${account.name}! Emitting result...") }
        .also { if (it.isEmpty()) loggedIn.postValue(false) }
        .also { accountsListState.postValue(it) }

    @WorkerThread
    suspend fun getPersonData(account: Account): PersonData? = database
        .peopleDao()
        .getDataByNif(account.name)

    /**
     * Stores the given credentials into the account manager.
     *
     * Updates [loggedIn].
     * @author Arnau Mora
     * @since 20221011
     * @param personData The [PersonData] of the user.
     * @param password The password that matches the one of the given user.
     * @return `true` if the account was added successfully, `false` otherwise.
     */
    @WorkerThread
    fun addAccount(personData: PersonData, password: String, token: String): Boolean {
        val account = Account(personData.nif, ACCOUNT_TYPE)
        val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            accountManager.addAccountExplicitly(
                account,
                password,
                null,
                mapOf(
                    BuildConfig.APPLICATION_ID to AccountManager.VISIBILITY_VISIBLE,
                ),
            ).also { if (it) loggedIn.postValue(true) }
                .also { getAccounts() }
        else
            accountManager.addAccountExplicitly(
                account,
                password,
                null,
            ).also { if (it) loggedIn.postValue(true) }
                .also { getAccounts() }
        if (!success)
            return false
        accountManager.setAuthToken(account, BuildConfig.AUTH_TOKEN_TYPE, token)
        return true
    }

    @WorkerThread
    fun setPassword(accountData: PersonData, password: String) =
        accountManager.setPassword(Account(accountData.nif, ACCOUNT_TYPE), password)

    /**
     * Removes the specified account.
     * @author Arnau Mora
     * @since 20221011
     * @param account The account to remove.
     * @param activity The activity that is requesting the removal.
     * @param timeout The timeout is seconds to wait until giving up on the removal.
     * @return `true` if the removal was successful, `false` otherwise.
     */
    @WorkerThread
    fun removeAccount(account: Account, activity: Activity, timeout: Long = 10): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            accountManager
                .removeAccount(account, activity, {}, Handler(activity.mainLooper))
                .getResult(timeout, TimeUnit.SECONDS)
                .getBoolean(KEY_BOOLEAN_RESULT)
                .also { if (it) notifyAccountRemoved(account) }
        else
            @Suppress("DEPRECATION")
            accountManager
                .removeAccount(account, {}, Handler(activity.mainLooper))
                .getResult(timeout, TimeUnit.SECONDS)
                .also { if (it) notifyAccountRemoved(account) }

    /**
     * Gets the stored token of the given account.
     * @author Arnau Mora
     * @since 20221025
     * @param account The account to get the token from.
     * @param timeout The maximum amount of time to wait until the resolution of the token.
     * @return The token stored in the account.
     * @throws OperationCanceledException If the operation timed out.
     * @throws NullPointerException If the returned auth token data doesn't contain a valid token.
     */
    @Throws(OperationCanceledException::class, NullPointerException::class)
    fun getToken(
        account: Account,
        timeout: Pair<Long, TimeUnit> = 10L to TimeUnit.SECONDS
    ): String {
        val tokenBundle = accountManager.getAuthToken(
            account,
            BuildConfig.AUTH_TOKEN_TYPE,
            null,
            true,
            null,
            null,
        ).getResult(timeout.first, timeout.second)
        return tokenBundle.getString(KEY_AUTHTOKEN)!!
    }
}
