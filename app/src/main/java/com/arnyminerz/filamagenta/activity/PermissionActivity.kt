package com.arnyminerz.filamagenta.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.R
import com.arnyminerz.filamagenta.ui.theme.setContentThemed
import timber.log.Timber

class PermissionActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_PERMISSIONS = "permissions"

        const val RESULT_BAD_REQUEST = 10
    }

    private val permissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
            if (granted.all { it.value }) {
                setResult(RESULT_OK)
                finish()
            } else
                Timber.w("Not all permissions granted: $granted")
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra(EXTRA_TITLE)
        val message = intent.getStringExtra(EXTRA_MESSAGE)
        val permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS)

        if (title == null || message == null || permissions == null || permissions.isEmpty()) {
            setResult(RESULT_BAD_REQUEST)
            finish()
            return
        }

        setContentThemed {
            BackHandler {
                setResult(RESULT_CANCELED)
                finish()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp, top = 128.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp, top = 16.dp),
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = { permissionRequest.launch(permissions) },
                ) {
                    Text(stringResource(R.string.permission_grant))
                }
            }
        }
    }

    data class PermissionRequest(
        val title: String,
        val message: String,
        val permissions: List<String>,
    )

    class Contract : ActivityResultContract<PermissionRequest, Int>() {
        override fun createIntent(context: Context, input: PermissionRequest): Intent =
            Intent(context, PermissionActivity::class.java).apply {
                putExtra(EXTRA_TITLE, input.title)
                putExtra(EXTRA_MESSAGE, input.message)
                putExtra(EXTRA_TITLE, input.permissions.toTypedArray())
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Int = resultCode
    }
}