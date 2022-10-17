package com.arnyminerz.filamagenta.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.data.account.Permission
import com.arnyminerz.filamagenta.ui.navigation.ComposeBottomAppBar
import com.arnyminerz.filamagenta.ui.navigation.NavItem
import com.arnyminerz.filamagenta.ui.pages.EventsPage
import com.arnyminerz.filamagenta.ui.pages.SettingsPage
import com.arnyminerz.filamagenta.ui.theme.JostFontFamily
import com.arnyminerz.filamagenta.ui.viewmodel.MainViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@Composable
@ExperimentalPagerApi
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
fun MainActivity.MainScreen(
    snackbarHostState: SnackbarHostState,
) {
    val pagerState = rememberPagerState()

    val account by viewModel.accountData.observeAsState()

    val items = listOf(
        NavItem(Icons.Rounded.Money, R.string.nav_accounting),
        NavItem(Icons.Rounded.Event, R.string.nav_events),
        NavItem(Icons.Rounded.Settings, R.string.nav_settings),
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontFamily = JostFontFamily,
                        fontSize = 28.sp,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                actions = {
                    Image(
                        painter = painterResource(R.drawable.logo_magenta),
                        contentDescription = stringResource(R.string.image_desc_account),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable(enabled = account != null) {
                                navController.navigate("account/${account?.username}")
                            },
                        contentScale = ContentScale.FillHeight,
                    )
                },
                navigationIcon = {
                    if (pagerState.currentPage == 1 && account?.hasPermission(Permission.EVENT_ADD) == true)
                        IconButton(onClick = { navController.navigate(MainActivity.Paths.EventAdd) }) {
                            Icon(
                                Icons.Rounded.Add,
                                stringResource(R.string.image_desc_add_event),
                            )
                        }
                },
            )
        },
        bottomBar = { items.ComposeBottomAppBar(state = pagerState) },
    ) { paddingValues ->
        HorizontalPager(
            count = items.size,
            state = pagerState,
            modifier = Modifier
                .padding(paddingValues),
        ) { page ->
            when (page) {
                0 -> Text("TODO: Comptabilitat")
                1 -> EventsPage()
                2 -> SettingsPage()
            }
        }
    }
}
