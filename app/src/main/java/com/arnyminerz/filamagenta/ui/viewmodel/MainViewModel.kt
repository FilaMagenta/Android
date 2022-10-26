package com.arnyminerz.filamagenta.ui.viewmodel

import android.accounts.Account
import android.app.Activity
import android.app.Application
import androidx.annotation.WorkerThread
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.auth.AccountSingleton
import com.arnyminerz.filamagenta.data.account.AccountData
import com.arnyminerz.filamagenta.data.event.TableData
import com.arnyminerz.filamagenta.database.local.AppDatabase
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import com.arnyminerz.filamagenta.database.local.entity.ShortPersonData
import com.arnyminerz.filamagenta.database.remote.RemoteInterface
import com.arnyminerz.filamagenta.utils.launchIO
import com.arnyminerz.filamagenta.utils.ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val selectedAccountIndex = MutableLiveData(0)

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
     * the value is mapped to an [AccountData].
     * @author Arnau Mora
     * @since 20221016
     */
    val accountData: LiveData<AccountData?> = Transformations.map(account) {
        findAccountDataByName(it.name)
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
    fun load(navController: NavController, force: Boolean = false) =
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
                            synchronizeEvents(token)
                        }
                    } catch (e: SQLException) {
                        if (e.message?.contains("network", true) == true)
                            navController.popBackStack(MainActivity.Paths.Error, true)
                        else
                            throw e
                    }
                }
                .also {
                    Timber.d("Finished loading data. Navigating...")
                    ui {
                        if (it.isNotEmpty())
                            navController.navigate(MainActivity.Paths.Main)
                        else
                            navController.navigate(MainActivity.Paths.Login)
                    }
                }
        }

    //<editor-fold desc="Authentication">
    fun tryToLogIn(
        navController: NavController,
        dni: String,
        password: String,
        snackbarHostState: SnackbarHostState
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
            val accountData = remote.getAccountData(token)

            val accountAdded = accountSingleton.addAccount(
                accountData,
                password,
                token,
            )
            if (!accountAdded)
                return@launchIO Timber.e("Could not add account. Unknown error.")

            synchronizeEvents(token)

            ui { navController.navigate(MainActivity.Paths.Main) }
        } catch (e: SQLException) {
            Timber.e(e, "Could not log in.")

            if (e.message?.contains("network", true) == true) {
                Timber.e(e, "No internet connection detected.")

                snackbarHostState.showSnackbar(
                    getApplication<Application>().getString(R.string.toast_no_internet)
                )
            } else
                snackbarHostState.showSnackbar(
                    getApplication<Application>().getString(R.string.toast_error_unknown)
                )
        }
    }

    fun signOut(accountData: AccountData, activity: Activity) =
        viewModelScope.launch(Dispatchers.IO) {
            Timber.w("Removing account ${accountData.username}...")
            val account = accountSingleton
                .getAccounts()
                .find { it.name == accountData.username }
                ?: run {
                    Timber.e("Could not find account with name ${accountData.username}")
                    return@launch
                }
            val accountRemoved = accountSingleton.removeAccount(account, activity)
            Timber.i("Account ${accountData.name} removed? $accountRemoved")
        }

    fun findAccountDataByName(name: String) =
        accountSingleton.getAccounts()
            .map { accountSingleton.getUserData(it) }
            .find { it.username == name }

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
    @Throws(SQLException::class)
    private suspend fun fetchEvents(token: String): List<EventEntity> = remote.getEvents(token)

    /**
     * Fetches all events from the remote database, and synchronizes them with the local one.
     * @author Arnau Mora
     * @since 20221015
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class)
    suspend fun synchronizeEvents(token: String) {
        val dao = appDatabase.eventsDao()
        val local = dao.getAll()

        val events = fetchEvents(token)
        // Add new events to the local database
        events.forEach { event ->
            val found = local.find { it.hashCode() == event.hashCode() } != null
            val isAnUpdate = found && local.find { it.id == event.id } != null

            if (!found) {
                Timber.v("Storing event#${event.id}...")
                dao.add(event)
            } else if (isAnUpdate) {
                Timber.v("Updating event#${event.id}...")
                dao.update(event)
            }
        }
        // Remove non-existing events
        local.forEach { event ->
            val found = events.find { it.hashCode() == event.hashCode() }
            // If not found on the remote, remove it
            if (found == null) {
                Timber.v("Removing event#${event.id}...")
                dao.remove(event)
            }
        }
    }

    /**
     * Creates a new table for a given event.
     * @author Arnau Mora
     * @since 20221015
     * @param responsible The responsible of the table.
     * @param event The event for which the table will be created.
     */
    fun createNewTable(
        responsible: AccountData,
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
    fun addToTable(event: EventEntity, tableIndex: Int, person: AccountData) =
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
    fun confirmAssistance(event: EventEntity, person: AccountData) =
        viewModelScope.launch(Dispatchers.IO) {
            TODO("Assistance confirmation in REST API")
        }
}