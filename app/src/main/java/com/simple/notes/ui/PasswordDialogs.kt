package com.simple.notes.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/** Новый блокнот: пароль вводится дважды, чтобы опечатка не заперла его навсегда. */
@Composable
fun NewProfileDialog(
    busy: Boolean,
    error: String?,
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый блокнот") },
        text = {
            Column {
                Text(
                    "Он будет пустым и откроется по этому паролю.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                DialogPasswordField(password, { password = it }, "Пароль", visible, { visible = !visible })
                Spacer(Modifier.height(12.dp))
                DialogPasswordField(
                    confirmation, { confirmation = it }, "Повторите пароль", visible, { visible = !visible },
                    imeAction = ImeAction.Done
                )
                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(password, confirmation) }, enabled = !busy) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

/** Смена пароля открытого блокнота: старый один раз, новый дважды. */
@Composable
fun ChangePasswordDialog(
    busy: Boolean,
    error: String?,
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Сменить пароль") },
        text = {
            Column {
                Text(
                    "Пароль этого блокнота. Заметки и фотографии останутся на месте, " +
                        "старый пароль работать перестанет.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                DialogPasswordField(oldPassword, { oldPassword = it }, "Старый пароль", visible, { visible = !visible })
                Spacer(Modifier.height(12.dp))
                DialogPasswordField(password, { password = it }, "Новый пароль", visible, { visible = !visible })
                Spacer(Modifier.height(12.dp))
                DialogPasswordField(
                    confirmation, { confirmation = it }, "Новый пароль ещё раз", visible, { visible = !visible },
                    imeAction = ImeAction.Done
                )
                if (error != null) {
                    Spacer(Modifier.height(12.dp))
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(oldPassword, password, confirmation) }, enabled = !busy) {
                Text("Сменить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun DialogPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
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
