package com.arnyminerz.filamagenta.activity

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import com.arnyminerz.filamagenta.R
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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import timber.log.Timber

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

    private val requestPermissionLauncher = registerForActivityResult(
        PermissionActivity.Contract()
    ) { result ->
        Timber.i("Permission result: %d", result)
    }

    @OptIn(
        ExperimentalPagerApi::class,
        ExperimentalMaterial3Api::class,
        ExperimentalComposeUiApi::class,
        ExperimentalAnimationApi::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        askNotificationPermission()
        checkForGooglePlayServices()

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

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PERMISSION_GRANTED
            ) requestPermissionLauncher.launch(
                PermissionActivity.PermissionRequest(
                    getString(R.string.permission_notifications_title),
                    getString(R.string.permission_notifications_message),
                    listOf(Manifest.permission.POST_NOTIFICATIONS),
                )
            )
    }

    private fun checkForGooglePlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        return if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status))
                googleApiAvailability.getErrorDialog(this, status, 2404)?.show()
            false
        } else
            true
    }
}
