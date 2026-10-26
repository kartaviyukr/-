package com.kartaviy.tgagent.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kartaviy.tgagent.App
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

@Composable
fun LoginScreen(state: TdApi.AuthorizationState, onSettings: () -> Unit) {
    val tg = App.instance.tg
    val scope = rememberCoroutineScope()
    var input by remember(state) { mutableStateOf("") }
    var error by remember(state) { mutableStateOf<String?>(null) }
    var busy by remember(state) { mutableStateOf(false) }

    val (title, hint, label, keyboard, secret) = when (state) {
        is TdApi.AuthorizationStateWaitPhoneNumber -> LoginStep(
            "Вход в Telegram", "Номер телефона в международном формате, например +79991234567.",
            "Телефон", KeyboardType.Phone, false,
        )
        is TdApi.AuthorizationStateWaitCode -> LoginStep(
            "Код подтверждения", "Telegram прислал код в приложение Telegram (или SMS). Введите его здесь.",
            "Код", KeyboardType.Number, false,
        )
        is TdApi.AuthorizationStateWaitPassword -> LoginStep(
            "Облачный пароль",
            "У аккаунта включена двухэтапная проверка." +
                (state.passwordHint.takeIf { it.isNotBlank() }?.let { " Подсказка: $it" } ?: ""),
            "Пароль", KeyboardType.Password, true,
        )
        else -> LoginStep(
            "Нужно действие в Telegram",
            "Этот способ входа пока не поддерживается (${state.javaClass.simpleName}). " +
                "Войдите в аккаунт в официальном Telegram и попробуйте снова.",
            null, KeyboardType.Text, false,
        )
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().imePadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(hint, style = MaterialTheme.typography.bodyMedium)
            if (label != null) {
                OutlinedTextField(
                    input, { input = it }, Modifier.fillMaxWidth(),
                    label = { Text(label) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                    visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    enabled = !busy && input.isNotBlank(),
                    onClick = {
                        busy = true
                        error = null
                        scope.launch {
                            try {
                                when (state) {
                                    is TdApi.AuthorizationStateWaitPhoneNumber -> tg.setPhone(input.trim())
                                    is TdApi.AuthorizationStateWaitCode -> tg.checkCode(input.trim())
                                    is TdApi.AuthorizationStateWaitPassword -> tg.checkPassword(input)
                                    else -> Unit
                                }
                            } catch (e: Exception) {
                                error = e.message
                            } finally {
                                busy = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (busy) CircularProgressIndicator(Modifier.padding(2.dp)) else Text("Далее")
                }
            }
            TextButton(onClick = onSettings) { Text("Настройки") }
        }
    }
}

private data class LoginStep(
    val title: String,
    val hint: String,
    val label: String?,
    val keyboard: KeyboardType,
    val secret: Boolean,
)

@Composable
fun LoadingScreen(onSettings: () -> Unit) {
    val error by App.instance.tg.lastError.collectAsState()
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            CircularProgressIndicator()
            Text("Подключаюсь к Telegram…")
            error?.let {
                Text("Ошибка: $it", color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onSettings) { Text("Проверить настройки") }
            }
        }
    }
}
