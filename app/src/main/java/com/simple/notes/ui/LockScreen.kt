package com.simple.notes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Экран ввода пароля. Здесь никогда не показывается ошибка «неверный пароль». */
@Composable
fun LockScreen(busy: Boolean, message: String?, onUnlock: (String) -> Unit) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().imePadding(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Заметки", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

            if (message != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(32.dp))

            PasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Пароль",
                visible = visible,
                onToggleVisibility = { visible = !visible },
                imeAction = ImeAction.Go,
                onSubmit = { onUnlock(password) }
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onUnlock(password) },
                enabled = password.isNotEmpty() && !busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Открыть")
                }
            }
        }
    }
}

/**
 * Первый запуск: пользователь задаёт пароли своих блокнотов — сколько нужно.
 * Ещё блокноты можно добавить позже, изнутри приложения.
 */
@Composable
fun SetupScreen(busy: Boolean, error: String?, onCreate: (List<String>) -> Unit) {
    val passwords = remember { mutableStateListOf("") }
    var visible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))
        Text("Придумайте пароли", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Каждый пароль открывает свой отдельный блокнот. Сколько паролей — столько блокнотов.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        passwords.forEachIndexed { index, value ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PasswordField(
                    value = value,
                    onValueChange = { passwords[index] = it },
                    label = "Пароль блокнота ${index + 1}",
                    visible = visible,
                    onToggleVisibility = { visible = !visible },
                    imeAction = if (index == passwords.lastIndex) ImeAction.Go else ImeAction.Next,
                    onSubmit = { onCreate(passwords.toList()) },
                    modifier = Modifier.weight(1f)
                )
                if (passwords.size > 1) {
                    IconButton(onClick = { passwords.removeAt(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Убрать блокнот")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        TextButton(onClick = { passwords.add("") }) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Добавить ещё блокнот")
        }

        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { onCreate(passwords.toList()) },
            enabled = !busy,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (passwords.size == 1) "Создать блокнот" else "Создать блокноты") }

        Spacer(Modifier.height(28.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(Modifier.padding(16.dp)) {
                Text("Как это работает", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Блокноты независимы: заметки одного не видны из другого. Любой пароль, " +
                        "которого нет в этом списке, откроет общий блокнот с посторонними " +
                        "заметками — приложение никогда не сообщает, что пароль неверный.\n\n" +
                        "Пароли нигде не сохраняются и не восстанавливаются. Нажмите на значок " +
                        "глаза и проверьте, что набрали именно то, что хотели.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    imeAction: ImeAction,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onGo = { onSubmit() },
            onDone = { onSubmit() }
        ),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "Показать пароль"
                )
            }
        }
    )
}
