package com.arnyminerz.filamagenta.ui.viewmodel

import android.accounts.Account
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.*
import androidx.navigation.NavController
import com.android.volley.NoConnectionError
import com.android.volley.VolleyError
import com.arnyminerz.filamagenta.App
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.activity.MainActivity.Companion.EXTRA_ACCOUNT_TYPE
import com.arnyminerz.filamagenta.auth.AccountSingleton
import com.arnyminerz.filamagenta.data.ACCOUNT_INDEX
import com.arnyminerz.filamagenta.data.LAST_EVENTS_SYNC
import com.arnyminerz.filamagenta.data.event.TableData
import com.arnyminerz.filamagenta.database.local.AppDatabase
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import com.arnyminerz.filamagenta.database.local.entity.PersonData
import com.arnyminerz.filamagenta.database.local.entity.ShortPersonData
import com.arnyminerz.filamagenta.database.remote.RemoteInterface
import com.arnyminerz.filamagenta.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import timber.log.Timber
import java.sql.SQLException

class MainViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * An instance of the app's local database.
     * @author Arnau Mora
     * @since 20221014
     */
    private val appDatabase = AppDatabase.getInstance(application)

    private val remote = RemoteInterface.getInstance(application)

    /**
     * A reference to the singleton that allows interaction with the device's accounts.
     * @author Arnau Mora
     * @since 20221014
     */
    private var accountSingleton = AccountSingleton.getInstance(application)

    /**
     * Returns a [LiveData] that gets updated with the current accounts list that have been logged
     * in.
     * @author Arnau Mora
     * @since 20221016
     */
    val accountsList = accountSingleton.accountsList

    /**
     * Returns an state that holds which profile from [accountsList] is currently selected by the
     * user.
     * @author Arnau Mora
     * @since 20221016
     */
    val selectedAccountIndex = application.getIntPreferences(ACCOUNT_INDEX).asLiveData()

    /**
     * Maps [accountsList] to [selectedAccountIndex] and returns a [LiveData] that gets the currently
     * selected account, and updates automatically with [accountsList] or [selectedAccountIndex].
     * @author Arnau Mora
     * @since 20221016
     */
    val account: LiveData<Account> = Transformations.switchMap(selectedAccountIndex) { index ->
        Transformations.map(accountsList) { accounts -> accounts[index] }
    }

    /**
     * Maps the value from [account], but runs [findAccountDataByName] before returning. This way
     * the value is mapped to a [PersonData].
     * @author Arnau Mora
     * @since 20221016
     */
    val accountData: LiveData<PersonData?> = Transformations.map(account) { account ->
        runBlocking { accountSingleton.getPersonData(account) }
    }

    /**
     * A live data object observing the contents of the events' database table.
     * @author Arnau Mora
     * @since 20221015
     */
    val eventsList = appDatabase.eventsDao().getAllLive()

    /**
     * Loads all the required data to start displaying the app to the user. This includes all the
     * accounts and events.
     * @author Arnau Mora
     * @since 20221014
     * @param navController The nav controller to update when finished loading.
     */
    @Throws(SQLException::class)
    fun load(navController: NavController, extras: Bundle?, force: Boolean = false) =
        viewModelScope.launch(Dispatchers.IO) {
            if (!force && !accountsList.value.isNullOrEmpty())
                return@launch Timber.d("Not loading since data already present.")

            Timber.d("Getting accounts...")
            accountSingleton.getAccounts()
                .also { accounts ->
                    try {
                        Timber.d("Got accounts. Synchronizing events for each account...")
                        for (account in accounts) {
                            val token = accountSingleton.getToken(account)
                            Timber.d("Synchronizing data for ${account.name}...")
                            remote.getPersonData(token)
                            Timber.d("Synchronizing events for ${account.name}...")
                            synchronizeEvents(token)
                        }
                    } catch (e: SQLException) {
                        if (e.message?.contains("network", true) == true)
                            navController.popBackStack(MainActivity.Paths.Error, true)
                        else
                            throw e
                    } catch (e: IllegalStateException) {
                        Timber.w(e, "Got invalid event.")
                    }
                }
                .also {
                    Timber.d(
                        "Finished loading data. Extras: %s. Accounts count: %d",
                        extras?.toMap(),
                        it.size,
                    )
                    ui {
                        if (it.isEmpty() || extras?.containsKey(EXTRA_ACCOUNT_TYPE) == true)
                            navController.navigate(MainActivity.Paths.Login)
                        else
                            navController.navigate(MainActivity.Paths.Main)
                    }
                }
        }

    //<editor-fold desc="Authentication">
    fun tryToLogIn(
        navController: NavController,
        dni: String,
        password: String,
        snackbarHostState: SnackbarHostState,
        addingNewAccount: Boolean,
    ) = launchIO {
        try {
            val token = remote.logIn(dni, password)
            Timber.i("Login valid: $token")
            /*if (!loginValid) {
                Timber.e("Invalid credentials.")
                snackbarHostState.showSnackbar(
                    getApplication<Application>().getString(R.string.toast_login_wrong_credentials)
                )
                return@launchIO
            }*/
            val accountData = remote.getPersonData(token)

            if (addingNewAccount) {
                Timber.i("Adding new account (%s).", accountData.nif)
                val accountAdded = accountSingleton.addAccount(
                    accountData,
                    password,
                    token,
                )
                if (!accountAdded)
                    return@launchIO Timber.e("Could not add account. Unknown error.")
            } else
                accountSingleton.setPassword(accountData, password)

            synchronizeEvents(token)

            ui { navController.navigate(MainActivity.Paths.Main) }
        } catch (e: VolleyError) {
            Timber.e(
                e,
                "Could not log in. Response (%d): %s",
                e.networkResponse?.statusCode ?: -1,
                e.networkResponse?.data?.toString(Charsets.UTF_8) ?: "N/A",
            )

            val code = e.networkResponse?.statusCode
            val msg = getApplication<Application>().getString(
                if (e is NoConnectionError)
                    R.string.toast_login_server
                else
                    when (code) {
                        403 -> R.string.toast_login_wrong_credentials
                        404 -> R.string.toast_login_not_found
                        412 -> R.string.toast_login_max_attempts
                        else -> R.string.toast_error_unknown
                    }
            )
            snackbarHostState.showSnackbar(msg)

            /*if (e is ConnectException) {
                Timber.e(e, "No internet connection detected.")

                snackbarHostState.showSnackbar(
                    getApplication<Application>().getString(R.string.toast_no_internet)
                )
            }*/
        } catch (e: JSONException) {
            Timber.e(e, "Could not parse response.")
            snackbarHostState.showSnackbar(
                getApplication<Application>().getString(R.string.toast_error_unknown)
            )
        } catch (e: IllegalStateException) {
            Timber.w(e, "Got invalid event.")
        }
    }

    fun signOut(accountData: PersonData, activity: Activity) =
        viewModelScope.launch(Dispatchers.IO) {
            Timber.w("Removing account ${accountData.nif}...")
            val account = accountSingleton
                .getAccounts()
                .find { it.name == accountData.nif }
                ?: run {
                    Timber.e("Could not find account with NIF ${accountData.nif}")
                    return@launch
                }
            val accountRemoved = accountSingleton.removeAccount(account, activity)
            Timber.i("Account ${accountData.name} removed? $accountRemoved")
        }

    fun findAccountDataByName(name: String) = mutableStateOf<PersonData?>(null).apply {
        launchIO {
            accountSingleton.getAccounts()
                .find { it.name == name }
                ?.let { accountSingleton.getPersonData(it) }
                ?.also { ui { value = it } }
                .also { if (it == null) Timber.d("Could not find an account named '%s'", name) }
        }
    }

    fun getAssistanceData(event: EventEntity): MutableLiveData<List<ShortPersonData>> =
        MutableLiveData<List<ShortPersonData>>().also {
            launchIO {
                it.postValue(
                    event.getAssistanceData(getApplication(), selectedAccountIndex.value ?: 0)
                )
            }
        }

    fun getMembersData(tableData: TableData): MutableLiveData<List<ShortPersonData>> =
        MutableLiveData<List<ShortPersonData>>().also {
            launchIO {
                it.postValue(
                    tableData.getMembersData(getApplication(), selectedAccountIndex.value ?: 0)
                )
            }
        }
    //</editor-fold>

    /**
     * Fetches all the defined events from the database.
     * @author Arnau Mora
     * @since 20221014
     * @return A list of [EventEntity] with the data obtained.
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class, IllegalStateException::class)
    private suspend fun fetchEvents(token: String): List<EventEntity> = remote.getEvents(token)

    /**
     * Fetches all events from the remote database, and synchronizes them with the local one.
     * @author Arnau Mora
     * @since 20221015
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class, IllegalStateException::class)
    suspend fun synchronizeEvents(token: String) {
        val dao = appDatabase.eventsDao()
        val local = dao.getAll()

        val events = fetchEvents(token)
        // Add new events to the local database
        events.forEach { event ->
            val found = local.find { it.hashCode() == event.hashCode() }
            val isAnUpdate = local.find { it.id == event.id } != null

            if (found == null && !isAnUpdate) {
                Timber.v("Storing event#${event.id}...")
                dao.add(event)
            } else if (isAnUpdate) {
                Timber.v("Updating event#${event.id}...")
                dao.update(event)
            }
        }

        // Remove non-existing events
        local.forEach { event ->
            val found = events.find { it.id == event.id }
            // If not found on the remote, remove it
            if (found == null) {
                Timber.v("Removing event#${event.id}...")
                dao.remove(event)
            }
        }

        getApplication<App>().dataStore.edit { it[LAST_EVENTS_SYNC] = System.currentTimeMillis() }
    }

    /**
     * Creates a new table for a given event.
     * @author Arnau Mora
     * @since 20221015
     * @param responsible The responsible of the table.
     * @param event The event for which the table will be created.
     */
    fun createNewTable(
        responsible: PersonData,
        event: EventEntity,
    ) = viewModelScope.launch(Dispatchers.IO) {
        TODO("Table creation in REST API")
    }

    /**
     * Adds a given person into a table.
     * @author Arnau Mora
     * @since 20221015
     * @param table The table to add into.
     * @param person The person to add into the table.
     */
    fun addToTable(event: EventEntity, tableIndex: Int, person: PersonData) =
        viewModelScope.launch(Dispatchers.IO) {
            TODO("Table joining in REST API")
        }

    /**
     * Confirms the assistance of a given person to a given event.
     * @author Arnau Mora
     * @since 20221015
     * @param event The event to confirm the assistance to.
     * @param person The person that is doing the confirmation.
     */
    fun confirmAssistance(event: EventEntity, person: PersonData) =
        viewModelScope.launch(Dispatchers.IO) {
            TODO("Assistance confirmation in REST API")
        }
}