package com.kartaviy.tgagent.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kartaviy.tgagent.App
import com.kartaviy.tgagent.Prefs
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

@Composable
fun SettingsScreen(canClose: Boolean, onDone: () -> Unit) {
    val app = App.instance
    val prefs = app.prefs
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var apiId by remember { mutableStateOf(if (prefs.tgApiId == 0) "" else prefs.tgApiId.toString()) }
    var apiHash by remember { mutableStateOf(prefs.tgApiHash) }
    var aiKey by remember { mutableStateOf(prefs.aiApiKey) }
    var baseUrl by remember { mutableStateOf(prefs.aiBaseUrl) }
    var model by remember { mutableStateOf(prefs.aiModel) }
    var privateMode by remember { mutableStateOf(prefs.privateMode) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Настройки", style = MaterialTheme.typography.headlineSmall)

            Text("Telegram API", style = MaterialTheme.typography.titleMedium)
            Text(
                "Откройте my.telegram.org → API development tools, создайте приложение и скопируйте api_id и api_hash.",
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://my.telegram.org/apps")))
            }) { Text("Открыть my.telegram.org") }
            OutlinedTextField(
                apiId, { apiId = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(),
                label = { Text("api_id") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                apiHash, { apiHash = it.trim() }, Modifier.fillMaxWidth(),
                label = { Text("api_hash") }, singleLine = true,
            )

            HorizontalDivider()
            Text("ИИ (DeepSeek)", style = MaterialTheme.typography.titleMedium)
            Text(
                "Ключ берётся на platform.deepseek.com → API keys. Хранится только на этом телефоне.",
                style = MaterialTheme.typography.bodySmall,
            )
            OutlinedTextField(
                aiKey, { aiKey = it.trim() }, Modifier.fillMaxWidth(),
                label = { Text("API-ключ") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
            )
            OutlinedTextField(
                baseUrl, { baseUrl = it.trim() }, Modifier.fillMaxWidth(),
                label = { Text("Адрес API") }, singleLine = true,
            )
            OutlinedTextField(
                model, { model = it.trim() }, Modifier.fillMaxWidth(),
                label = { Text("Модель") }, singleLine = true,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Приватный режим")
                    Text(
                        "Текст найденных сообщений не отправляется в ИИ — только дата, отправитель и тип.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(privateMode, { privateMode = it })
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            info?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Button(
                onClick = {
                    val id = apiId.toIntOrNull()
                    when {
                        id == null || id == 0 -> error = "Укажите api_id"
                        apiHash.isBlank() -> error = "Укажите api_hash"
                        aiKey.isBlank() -> error = "Укажите API-ключ ИИ"
                        else -> {
                            val tgChanged = app.tg.isStarted && (id != prefs.tgApiId || apiHash != prefs.tgApiHash)
                            prefs.tgApiId = id
                            prefs.tgApiHash = apiHash
                            prefs.aiApiKey = aiKey
                            prefs.aiBaseUrl = baseUrl.ifBlank { Prefs.DEFAULT_BASE_URL }
                            prefs.aiModel = model.ifBlank { Prefs.DEFAULT_MODEL }
                            prefs.privateMode = privateMode
                            if (tgChanged) {
                                error = null
                                info = "Сохранено. Новые api_id/api_hash применятся после перезапуска приложения."
                            } else {
                                onDone()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Сохранить") }

            if (canClose) {
                OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Назад") }
            }
            if (app.tg.authState.value is TdApi.AuthorizationStateReady) {
                TextButton(onClick = {
                    scope.launch { runCatching { app.tg.logOut() } }
                    onDone()
                }) { Text("Выйти из аккаунта Telegram", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
