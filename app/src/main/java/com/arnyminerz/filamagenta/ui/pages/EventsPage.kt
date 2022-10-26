package com.arnyminerz.filamagenta.ui.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.arnyminerz.filamagenta.activity.MainActivity
import com.arnyminerz.filamagenta.database.local.entity.assists
import com.arnyminerz.filamagenta.ui.reusable.EventCard
import com.arnyminerz.filamagenta.ui.reusable.LoadingIndicatorBox

@Composable
fun MainActivity.EventsPage() {
    val events by viewModel.eventsList.observeAsState(emptyList())

    val account by viewModel.accountData.observeAsState()

    if (account == null)
        LoadingIndicatorBox()
    else
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            items(events.sortedBy { it.date.time }) { event ->
                val assistanceConfirmed = events.assists(account!!.id)
                EventCard(
                    event = event,
                    assistanceConfirmed = assistanceConfirmed,
                    festerType = account!!.type,
                ) {
                    navController.navigate(
                        MainActivity.Paths.Event.replace("{event}", event.id.toString())
                    )
                }
            }
        }
}
