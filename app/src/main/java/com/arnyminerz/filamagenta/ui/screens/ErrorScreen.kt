package com.arnyminerz.filamagenta.ui.screens

import androidx.annotation.IntDef
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R

const val ERROR_UNKNOWN: Int = -1
const val ERROR_INTERNET: Int = 1

@IntDef(ERROR_UNKNOWN, ERROR_INTERNET)
annotation class Error

@Preview(
    showSystemUi = true,
    showBackground = true,
)
@Composable
fun ErrorScreen(@Error error: Int? = ERROR_UNKNOWN) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.error_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    when(error) {
                        ERROR_INTERNET -> R.string.error_internet
                        else -> R.string.error_unknown
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}
