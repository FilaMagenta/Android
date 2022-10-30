package com.arnyminerz.filamagenta.ui.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.database.local.AppDatabase
import com.arnyminerz.filamagenta.ui.reusable.LoadingIndicatorBox
import com.arnyminerz.filamagenta.ui.viewmodel.MainViewModel
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@ExperimentalAnimationApi
fun NavGraphBuilder.errorComposable() {
    composable(
        MainActivity.Paths.Error,
        arguments = listOf(
            navArgument("error") { type = NavType.StringType },
        ),
    ) { ErrorScreen(it.arguments?.getInt("error")) }
}

@ExperimentalAnimationApi
fun NavGraphBuilder.loadingComposable() {
    composable(
        MainActivity.Paths.Loading,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
    ) { LoadingIndicatorBox() }
}

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
fun NavGraphBuilder.loginComposable(
    viewModel: MainViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
) {
    composable(
        MainActivity.Paths.Login,
        enterTransition = {
            slideIntoContainer(AnimatedContentScope.SlideDirection.Down) + fadeIn()
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentScope.SlideDirection.Up) + fadeOut()
        },
    ) {
        LoginScreen(snackbarHostState) { username, password ->
            Timber.v("Logging in...")
            viewModel.tryToLogIn(navController, username, password, snackbarHostState)
        }
    }
}

@ExperimentalPagerApi
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
fun NavGraphBuilder.mainComposable(activity: MainActivity, snackbarHostState: SnackbarHostState) {
    composable(
        MainActivity.Paths.Main,
        enterTransition = {
            when (initialState.destination.route) {
                MainActivity.Paths.Login -> slideIntoContainer(AnimatedContentScope.SlideDirection.Left)
                MainActivity.Paths.Account -> null
                else -> fadeIn()
            }
        },
        exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Left) },
    ) { activity.MainScreen(snackbarHostState) }
}

@ExperimentalMaterial3Api
@ExperimentalAnimationApi
fun NavGraphBuilder.accountComposable(activity: MainActivity) {
    composable(
        MainActivity.Paths.Account,
        arguments = listOf(
            navArgument("account") { type = NavType.StringType },
        ),
        enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
        exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Right) },
    ) { entry ->
        val accountName = entry.arguments?.getString("account") ?: run {
            Timber.e("Account name is null.")
            activity.navController.navigateUp()
            return@composable
        }
        val account = activity.viewModel.findAccountDataByName(accountName) ?: run {
            Timber.e("Could not find account named $accountName")
            activity.navController.navigateUp()
            return@composable
        }

        activity.AccountScreen(account)
    }
}

@ExperimentalMaterial3Api
@ExperimentalAnimationApi
fun NavGraphBuilder.eventComposable(viewModel: MainViewModel, navController: NavController) {
    composable(
        MainActivity.Paths.Event,
        arguments = listOf(
            navArgument("event") { type = NavType.LongType },
        ),
        enterTransition = { slideIntoContainer(AnimatedContentScope.SlideDirection.Left) },
        exitTransition = { slideOutOfContainer(AnimatedContentScope.SlideDirection.Right) },
    ) { entry ->
        val context = LocalContext.current

        val eventId = entry.arguments?.getLong("event") ?: run {
            Timber.e("Event id is null.")
            navController.navigateUp()
            return@composable
        }
        val event = AppDatabase.getInstance(context)
            .eventsDao()
            // TODO: Do not block thread
            .let { runBlocking { it.findById(eventId) } }
            .takeIf { it.isNotEmpty() }
            ?.get(0) ?: run {
            Timber.e("Could not find event with id $eventId")
            navController.navigateUp()
            return@composable
        }

        val account by viewModel.accountData.observeAsState()
        if (account == null)
            LoadingIndicatorBox()
        else
            EventScreen(
                navController,
                viewModel,
                event = event,
            )
    }
}

@ExperimentalMaterial3Api
@ExperimentalAnimationApi
fun NavGraphBuilder.eventAddComposable(activity: MainActivity) {
    composable(MainActivity.Paths.EventAdd) {
        activity.EventAddScreen()
    }
}
