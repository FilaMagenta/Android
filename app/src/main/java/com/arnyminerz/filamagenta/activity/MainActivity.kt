package com.arnyminerz.filamagenta.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.arnyminerz.filamagenta.ui.screens.*
import com.arnyminerz.filamagenta.ui.theme.setContentThemed
import com.arnyminerz.filamagenta.ui.viewmodel.MainViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi

class MainActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ACCOUNT_TYPE = "account_type"
        const val EXTRA_AUTH_TOKEN_TYPE = "token_type"
        const val EXTRA_ADDING_NEW_ACCOUNT = "adding_new_account"
    }

    val viewModel: MainViewModel by viewModels()

    object Paths {
        const val Error = "error/{error}"

        const val Login = "login"

        const val Loading = "loading"

        const val Main = "main"

        const val Account = "account/{account}"

        const val Event = "event/{event}"

        const val EventAdd = "event_add"
    }

    lateinit var navController: NavHostController

    @OptIn(
        ExperimentalPagerApi::class,
        ExperimentalMaterial3Api::class,
        ExperimentalComposeUiApi::class,
        ExperimentalAnimationApi::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentThemed {
            navController = rememberAnimatedNavController()
            val snackbarHostState = remember { SnackbarHostState() }

            AnimatedNavHost(navController, startDestination = Paths.Loading) {
                errorComposable()
                loadingComposable()
                loginComposable(viewModel, navController, snackbarHostState, intent.extras)
                mainComposable(this@MainActivity, snackbarHostState)
                accountComposable(this@MainActivity)
                eventComposable(viewModel, navController)
                eventAddComposable(this@MainActivity)
            }

            viewModel.load(navController, intent.extras)
        }
    }

    override fun onResume() {
        super.onResume()

        if (this::navController.isInitialized)
            viewModel.load(navController, intent.extras, true)
    }
}
