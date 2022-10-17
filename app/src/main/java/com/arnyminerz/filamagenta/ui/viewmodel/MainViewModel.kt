package com.arnyminerz.filamagenta.ui.viewmodel

import android.accounts.Account
import android.app.Activity
import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.annotation.WorkerThread
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.data.account.AccountData
import com.arnyminerz.filamagenta.auth.AccountSingleton
import com.arnyminerz.filamagenta.data.account.Permission
import com.arnyminerz.filamagenta.database.local.AppDatabase
import com.arnyminerz.filamagenta.database.local.entity.EventEntity
import com.arnyminerz.filamagenta.database.local.entity.SocioEntity
import com.arnyminerz.filamagenta.database.local.entity.TableEntity
import com.arnyminerz.filamagenta.database.remote.Database
import com.arnyminerz.filamagenta.utils.serialize.mapDatabaseEntries
import com.arnyminerz.filamagenta.utils.ui
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.ConnectException
import java.sql.Date
import java.sql.SQLException
import java.util.concurrent.FutureTask

class MainViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * An instance of [Database] for fetching data from the remote database.
     * TODO: Replace parameters with external variables.
     * @author Arnau Mora
     * @since 20221014
     */
    private val database = Database(
        "51.91.58.126",
        "GesTro",
        "sa",
        "Magenta1865",
    )

    /**
     * An instance of the app's local database.
     * @author Arnau Mora
     * @since 20221014
     */
    private val appDatabase = AppDatabase.getInstance(application)

    /**
     * A reference to the singleton that allows interaction with the device's accounts.
     * @author Arnau Mora
     * @since 20221014
     */
    private var accountSingleton = AccountSingleton.getInstance(application)

    /**
     * Disconnects the database if connected.
     * @author Arnau Mora
     * @since 20221014
     */
    fun disconnect() = viewModelScope.launch(Dispatchers.IO) {
        if (!database.isDisconnected)
            database.close()
    }

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
     * A live data object observing the contents of the events' database table.
     * @author Arnau Mora
     * @since 20221015
     */
    val tablesList = appDatabase.eventsDao().getTablesLive()

    /**
     * A live data object observing the contents of the people' database table.
     * @author Arnau Mora
     * @since 20221015
     */
    val people = appDatabase.peopleDao().getAllLive()

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
                .also {
                    try {
                        Timber.d("Got accounts. Synchronizing events...")
                        synchronizeEvents()

                        Timber.d("Got events. Synchronizing tables...")
                        synchronizeTables()

                        Timber.d("Got events. Synchronizing socios...")
                        synchronizeSocios()
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
        username: String,
        password: String,
        snackbarHostState: SnackbarHostState
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val result = database.select(
                "tbSocios",
                mapOf(
                    "idSocio" to Long::class,
                    "Nombre" to String::class,
                    "Apellidos" to String::class,
                    "Direccion" to String::class,
                    "idCodPostal" to Int::class,
                    "Dni" to String::class,
                    "FecNacimiento" to Date::class,
                    "FecAlta" to Date::class,
                    "TlfParticular" to String::class,
                    "TlfTrabajo" to String::class,
                    "TlfMovil" to String::class,
                    "eMail" to String::class,
                    "Fotografia" to String::class,
                    "nrRodaNegros" to Int::class,
                    "nrRodaBlancos" to Int::class,
                    "nrAntiguedad" to Int::class,
                    "bCarnetAvancarga" to Boolean::class,
                    "bDisparaAvancarga" to Boolean::class,
                    "FecExpedicionAvancarga" to Date::class,
                    "FecCaducidadAvancarga" to Date::class,
                    "idTipoFestero" to Int::class,
                    "idFormaPago" to Int::class,
                ),
                mapOf(
                    "Nombre" to username.uppercase(),
                    "Dni" to password.uppercase(),
                ),
            )
            Timber.i("Login valid: ${result.isNotEmpty()}")
            if (result.size > 1) {
                Timber.e("Got more than one result in the database. UNEXPECTED")
                snackbarHostState.showSnackbar(
                    getApplication<Application>().getString(R.string.toast_error_unknown)
                )
                return@launch
            }
            if (result.isEmpty()) {
                Timber.e("Invalid credentials.")
                snackbarHostState.showSnackbar(
                    getApplication<Application>().getString(R.string.toast_login_wrong_credentials)
                )
                return@launch
            }

            Timber.i("Row: ${result[0]}")

            val level = database.select(
                "mPermission",
                mapOf("Level" to String::class),
            ).map { it["Level"] as String }
            val permissions = level.takeIf { it.any { p -> p == "*" } }
                ?.let { Permission.values().toList() }
                ?: level.map { it.split(",") }
                    .flatten()
                    .map { Permission.valueOf(it) }

            val accountAdded = accountSingleton.addAccount(
                result.mapDatabaseEntries(AccountData.Companion)[0].copy(permissions = permissions),
                password,
            )
            if (!accountAdded)
                return@launch Timber.e("Could not add account. Unknown error.")

            synchronizeEvents()
            synchronizeSocios()
            synchronizeTables()

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
    private fun fetchEvents(): List<EventEntity> =
        database.select(
            "mEvents",
            mapOf(
                "id" to Long::class,
                "DisplayName" to String::class,
                "Date" to Date::class,
                "Menu" to String::class,
                "Contact" to String::class,
                "Description" to String::class,
                "Category" to Long::class,
            ),
            null,
        ).mapDatabaseEntries(EventEntity.Companion).let { events ->
            val pairs = database.select(
                "mAssistance",
                mapOf(
                    "Id" to Long::class,
                    "Event" to Long::class,
                    "Person" to Long::class,
                ),
            ).map { map -> map["Event"] as Long to map["Person"] as Long }
            events.map { event ->
                event.copy(
                    // Get the ids of all the people that match the table id
                    assistance = pairs.filter { (eventId, _) -> event.dbId == eventId }
                        .map { it.second },
                )
            }
        }

    /**
     * Fetches all the defined tables from the database.
     * @author Arnau Mora
     * @since 20221015
     * @return A list of [TableEntity] with the data obtained.
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class)
    private fun fetchTables(): List<TableEntity> =
        database.select(
            "mTables",
            mapOf(
                "Id" to Long::class,
                "Responsible" to Long::class,
                "Event" to Long::class,
            ),
        ).mapDatabaseEntries(TableEntity.Companion).let { tables ->
            val pairs = database.select(
                "mTablesPeople",
                mapOf(
                    "Id" to Long::class,
                    "Person" to Long::class,
                    "TableId" to Long::class,
                ),
            ).map { map ->
                val person = map["Person"] as Long
                val table = map["TableId"] as Long
                person to table
            }
            tables.map { table ->
                table.copy(
                    // Get the ids of all the people that match the table id
                    people = pairs.filter { (_, dbTable) -> table.id == dbTable }.map { it.first },
                )
            }
        }

    /**
     * Fetches all the defined socios from the database.
     * @author Arnau Mora
     * @since 20221015
     * @return A list of [SocioEntity] with the data obtained.
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class)
    private fun fetchSocios(): List<SocioEntity> =
        database.select(
            "tbSocios",
            mapOf(
                "idSocio" to Long::class,
                "Nombre" to String::class,
                "Apellidos" to String::class,
                "Direccion" to String::class,
                "idCodPostal" to Int::class,
                "Dni" to String::class,
                "FecNacimiento" to Date::class,
                "FecAlta" to Date::class,
                "TlfParticular" to String::class,
                "TlfTrabajo" to String::class,
                "TlfMovil" to String::class,
                "eMail" to String::class,
                "Fotografia" to String::class,
                "nrRodaNegros" to Int::class,
                "nrRodaBlancos" to Int::class,
                "nrAntiguedad" to Int::class,
                "bCarnetAvancarga" to Boolean::class,
                "bDisparaAvancarga" to Boolean::class,
                "FecExpedicionAvancarga" to Date::class,
                "FecCaducidadAvancarga" to Date::class,
                "idTipoFestero" to Int::class,
                "idFormaPago" to Int::class,
            ),
        ).mapDatabaseEntries(SocioEntity.Companion)

    /**
     * Fetches all events from the remote database, and synchronizes them with the local one.
     * @author Arnau Mora
     * @since 20221015
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class)
    suspend fun synchronizeEvents() {
        val dao = appDatabase.eventsDao()
        val local = dao.getAll()

        val events = fetchEvents()
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
     * Fetches all tables from the remote database, and synchronizes them with the local one.
     * @author Arnau Mora
     * @since 20221015
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class)
    suspend fun synchronizeTables() {
        val dao = appDatabase.eventsDao()
        val local = dao.getAllTables()

        val tables = fetchTables()

        // Add new tables
        tables.forEach { table ->
            val found = local.find { it.hashCode() == table.hashCode() } != null
            val isAnUpdate = found && local.find { it.id == table.id } != null
            // If not found on the database add it
            if (!found) {
                Timber.v("Storing table#${table.id}...")
                dao.add(table)
            } else if (isAnUpdate) {
                Timber.v("Updating table#${table.id}g...")
                dao.updatePeople(table.id, table.people)
            }
        }
        // Remove non-existing tables
        local.forEach { table ->
            val found = tables.find { it.hashCode() == table.hashCode() }
            // If not found on the remote, remove it
            if (found == null) {
                Timber.v("Removing table#${table.id}...")
                dao.remove(table)
            }
        }
    }

    /**
     * Fetches all socios from the remote database, and synchronizes them with the local one.
     * @author Arnau Mora
     * @since 20221015
     * @throws SQLException If there's an error while running the SQL query.
     */
    @WorkerThread
    @Throws(SQLException::class)
    suspend fun synchronizeSocios() {
        val dao = appDatabase.peopleDao()
        val local = dao.getAll()

        // Add new events
        fetchSocios().forEach { socio ->
            if (!local.contains(socio)) try {
                Timber.v("Storing socio#${socio.id}...")
                dao.add(socio)
            } catch (e: SQLiteConstraintException) {
                Timber.e(e, "Could not add socio#${socio.id}. Already exists.")
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
        database.insert(
            "mTables",
            mapOf(
                "Responsible" to responsible.id,
                // Events are stored locally with an increased index, since Room starts indexing at
                // 1, but SQL Server starts at 0
                "Event" to event.dbId,
            ),
        )
        synchronizeTables()
    }

    /**
     * Adds a given person into a table.
     * @author Arnau Mora
     * @since 20221015
     * @param table The table to add into.
     * @param person The person to add into the table.
     */
    fun addToTable(table: TableEntity, person: AccountData) =
        viewModelScope.launch(Dispatchers.IO) {
            database.insert(
                "mTablesPeople",
                mapOf(
                    "Person" to person.id,
                    "TableId" to table.id,
                ),
            )
            synchronizeTables()
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
            database.insert(
                "mAssistance",
                mapOf(
                    "Event" to event.dbId,
                    "Person" to person.id,
                ),
            )
            synchronizeEvents()
        }
}