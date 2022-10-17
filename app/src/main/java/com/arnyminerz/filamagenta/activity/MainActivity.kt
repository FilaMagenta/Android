package com.arnyminerz.filamagenta.activity

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.arnyminerz.filamagenta.ui.screens.accountComposable
import com.arnyminerz.filamagenta.ui.screens.errorComposable
import com.arnyminerz.filamagenta.ui.screens.eventAddComposable
import com.arnyminerz.filamagenta.ui.screens.eventComposable
import com.arnyminerz.filamagenta.ui.screens.loadingComposable
import com.arnyminerz.filamagenta.ui.screens.loginComposable
import com.arnyminerz.filamagenta.ui.screens.mainComposable
import com.arnyminerz.filamagenta.ui.theme.setContentThemed
import com.arnyminerz.filamagenta.ui.viewmodel.MainViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi

class MainActivity : AppCompatActivity() {
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
                loginComposable(viewModel, navController, snackbarHostState)
                mainComposable(this@MainActivity, snackbarHostState)
                accountComposable(this@MainActivity)
                eventComposable(viewModel, navController)
                eventAddComposable(this@MainActivity)
            }

            viewModel.load(navController)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.disconnect()
    }

    override fun onResume() {
        super.onResume()

        if (this::navController.isInitialized)
            viewModel.load(navController, true)
    }
}
