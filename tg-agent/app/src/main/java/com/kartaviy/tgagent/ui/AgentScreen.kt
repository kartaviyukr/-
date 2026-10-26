package com.kartaviy.tgagent.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kartaviy.tgagent.agent.FoundMessage
import com.kartaviy.tgagent.agent.formatDate

private val examples = listOf(
    "Напомни мне завтра в 9:00 позвонить маме",
    "Найди все пересланные сообщения в чате с Петей за октябрь",
    "Создай папку «Работа» и положи туда все чаты со словом «проект»",
    "Какие у меня запланированные сообщения?",
)

@Composable
fun AgentScreen(onSettings: () -> Unit, vm: AgentViewModel = viewModel()) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(vm.items.size) {
        if (vm.items.isNotEmpty()) listState.animateScrollToItem(vm.items.size - 1)
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "TG Агент", style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                    )
                    TextButton(onClick = vm::newDialog, enabled = !vm.busy) { Text("Новый") }
                    TextButton(onClick = onSettings) { Text("Настройки") }
                }
            }
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        input, { input = it }, Modifier.weight(1f),
                        placeholder = { Text("Что сделать?") }, maxLines = 5,
                    )
                    Spacer(Modifier.padding(4.dp))
                    if (vm.busy) {
                        CircularProgressIndicator(Modifier.padding(8.dp))
                    } else {
                        Button(onClick = { vm.send(input.trim()); input = "" }, enabled = input.isNotBlank()) {
                            Text("➤")
                        }
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (vm.items.isEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Напишите, что сделать. Например:", style = MaterialTheme.typography.bodyMedium)
                        examples.forEach { ex ->
                            OutlinedButton(onClick = { input = ex }, modifier = Modifier.fillMaxWidth()) {
                                Text(ex)
                            }
                        }
                    }
                }
            }
            items(vm.items) { item -> ItemView(item, vm) }
        }
    }
}

@Composable
private fun Bubble(text: String, mine: Boolean) {
    Box(Modifier.fillMaxWidth(), contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 340.dp),
        ) {
            Text(text, Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun ItemView(item: Item, vm: AgentViewModel) {
    when (item) {
        is Item.User -> Bubble(item.text, mine = true)
        is Item.Bot -> Bubble(item.text, mine = false)
        is Item.Note -> Text(
            item.text, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is Item.Error -> Text("⚠ ${item.text}", color = MaterialTheme.colorScheme.error)
        is Item.Confirm -> ConfirmCard(item, vm)
        is Item.Results -> ResultsCard(item, vm)
    }
}

@Composable
private fun ConfirmCard(item: Item.Confirm, vm: AgentViewModel) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Подтвердите действие", fontWeight = FontWeight.Bold)
            Text(item.text)
            when (item.decided) {
                null -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.answer(item, true) }) { Text("Выполнить") }
                    OutlinedButton(onClick = { vm.answer(item, false) }) { Text("Отмена") }
                }
                true -> Text("✓ Подтверждено", color = MaterialTheme.colorScheme.primary)
                false -> Text("✕ Отменено", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun ResultsCard(item: Item.Results, vm: AgentViewModel) {
    var shown by remember { mutableIntStateOf(20) }
    Card {
        Column(Modifier.padding(12.dp)) {
            Text("Найдено: ${item.messages.size}", fontWeight = FontWeight.Bold)
            Text(item.title, style = MaterialTheme.typography.bodySmall)
            if (item.messages.isNotEmpty()) {
                Text(
                    "Нажмите — открыть в Telegram, удерживайте — скопировать текст.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item.messages.take(shown).forEach { m ->
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                MessageRow(m, vm)
            }
            if (item.messages.size > shown) {
                TextButton(onClick = { shown += 50 }) {
                    Text("Показать ещё (${item.messages.size - shown})")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageRow(m: FoundMessage, vm: AgentViewModel) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxWidth().combinedClickable(
            onClick = { vm.open(context, m) },
            onLongClick = { copy(context, m.text) },
        )
    ) {
        Text(
            "${m.chatTitle} · ${formatDate(m.date)}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        val meta = buildList {
            if (m.sender.isNotBlank()) add("от ${m.sender}")
            m.forwardedFrom?.let { add("↪ переслано от $it") }
            if (m.type != "text") add("[${m.type}]")
        }.joinToString("  ")
        if (meta.isNotBlank()) Text(meta, style = MaterialTheme.typography.labelSmall)
        if (m.text.isNotBlank()) {
            Text(m.text, maxLines = 4, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun copy(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("message", text))
    Toast.makeText(context, "Скопировано", Toast.LENGTH_SHORT).show()
}
