package com.arnyminerz.filamagenta.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch

data class NavItem(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
)

@Composable
@ExperimentalPagerApi
fun Iterable<NavItem>.ComposeBottomAppBar(state: PagerState) {
    val scope = rememberCoroutineScope()
    BottomAppBar {
        forEachIndexed { index, (icon, labelRes) ->
            NavigationBarItem(
                selected = state.currentPage == index,
                onClick = { scope.launch { state.animateScrollToPage(index) } },
                icon = { Icon(icon, stringResource(labelRes)) },
                label = { Text(stringResource(labelRes)) },
            )
        }
    }
}
