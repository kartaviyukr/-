package com.fantasymap.creator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fantasymap.creator.editor.EditorViewModel
import com.fantasymap.creator.model.Ability
import com.fantasymap.creator.model.Attack
import com.fantasymap.creator.model.CheckSkill
import com.fantasymap.creator.model.Dnd
import com.fantasymap.creator.model.Secret
import com.fantasymap.creator.model.Selection
import com.fantasymap.creator.model.Token
import com.fantasymap.creator.model.TokenFaction

/** Окна режима игры. */
enum class PlayDialog { ATTACK, CHECK, LOG, SHEET, INITIATIVE, DICE }

/** Нижняя панель режима игры: чей ход, выбранная фишка и действия. */
@Composable
fun PlayPanel(viewModel: EditorViewModel, onDialog: (PlayDialog) -> Unit) {
    val project = viewModel.project ?: return
    val active = viewModel.activeTokenId()?.let { id -> project.tokens.firstOrNull { it.id == id } }
    val selected = (viewModel.selection as? Selection.TokenSel)?.let { sel -> project.tokens.firstOrNull { it.id == sel.id } }
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 6.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (active != null) "Раунд ${project.scene.round} · ходит ${active.title}" else "Бой не начат",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    val focus = selected ?: active
                    if (focus != null) {
                        val sheet = Dnd.sheetOf(focus)
                        Text(
                            "${focus.title}: хиты ${focus.hp}/${focus.maxHp} · КД ${focus.ac} · скорость ${sheet.speed} фт" +
                                if (focus.conditions.isNotEmpty()) " · " + focus.conditions.joinToString { it.title } else "",
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        Text("Коснитесь фишки, чтобы выбрать, и ведите — покажет пройденные футы", style = MaterialTheme.typography.labelSmall)
                    }
                }
                TextButton(onClick = { viewModel.nextTurn() }) { Text("⏭ Ход") }
            }
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item { FilterChip(false, { onDialog(PlayDialog.ATTACK) }, label = { Text("⚔ Атака") }) }
                item { FilterChip(false, { onDialog(PlayDialog.CHECK) }, label = { Text("🔍 Проверка") }) }
                item { FilterChip(false, { onDialog(PlayDialog.DICE) }, label = { Text("🎲 Кубики") }) }
                item { FilterChip(false, { onDialog(PlayDialog.INITIATIVE) }, label = { Text("⏳ Инициатива") }) }
                item { FilterChip(false, { onDialog(PlayDialog.LOG) }, label = { Text("📜 Журнал") }) }
                if (selected != null) {
                    item { FilterChip(false, { viewModel.changeHp(selected.id, -1) }, label = { Text("−1 хит") }) }
                    item { FilterChip(false, { viewModel.changeHp(selected.id, 1) }, label = { Text("+1 хит") }) }
                    item { FilterChip(false, { onDialog(PlayDialog.SHEET) }, label = { Text("📋 Лист") }) }
                }
                item {
                    FilterChip(
                        project.style.playerView,
                        { viewModel.togglePlayerView() },
                        label = { Text(if (project.style.playerView) "👥 Экран игроков" else "🎩 Экран мастера") }
                    )
                }
                item { FilterChip(project.scene.vision, { viewModel.toggleVision() }, label = { Text("👁 Зрение героев") }) }
            }
        }
    }
}

private fun sortedTargets(project: com.fantasymap.creator.model.MapProject, from: Token?): List<Token> =
    project.tokens.filter { it.id != from?.id && !it.dead }
        .sortedBy { if (from == null) 0f else it.pos.distanceTo(from.pos) }

private fun feet(project: com.fantasymap.creator.model.MapProject, a: Token, b: Token): Int {
    val cells = kotlin.math.round(a.pos.distanceTo(b.pos) / project.gridCell).toInt()
    return cells * project.feetPerCell
}

@Composable
private fun AdvantageRow(advantage: Int, onChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(advantage == -1, { onChange(-1) }, label = { Text("Помеха") })
        FilterChip(advantage == 0, { onChange(0) }, label = { Text("Обычно") })
        FilterChip(advantage == 1, { onChange(1) }, label = { Text("Преимущество") })
    }
}

/** Атака: кто бьёт, чем и кого. Бросок против КД, урон сразу в хиты. */
@Composable
fun AttackDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
    val start = (viewModel.selection as? Selection.TokenSel)?.id ?: viewModel.activeTokenId()
    var attackerId by remember { mutableStateOf(start ?: project.tokens.firstOrNull()?.id) }
    val attacker = project.tokens.firstOrNull { it.id == attackerId }
    val attacks = attacker?.let { Dnd.sheetOf(it).attacks }.orEmpty()
    var attackIndex by remember { mutableIntStateOf(0) }
    var targetId by remember { mutableStateOf<String?>(null) }
    var advantage by remember { mutableIntStateOf(0) }
    var apply by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<String?>(null) }
    val targets = sortedTargets(project, attacker)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Атака") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                Text("Кто атакует", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (t in project.tokens.filter { !it.dead }) {
                        FilterChip(t.id == attackerId, { attackerId = t.id; attackIndex = 0 }, label = { Text(t.title) })
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Чем", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    attacks.forEachIndexed { i, a ->
                        FilterChip(i == attackIndex, { attackIndex = i }, label = { Text("${a.name} ${Dnd.signed(a.bonus)} · ${a.damage}") })
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Цель", style = MaterialTheme.typography.labelLarge)
                for (t in targets) {
                    Row(
                        Modifier.fillMaxWidth()
                            .background(if (t.id == targetId) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .clickable { targetId = t.id }
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ColorDot(Color(t.faction.color))
                        Spacer(Modifier.width(6.dp))
                        Text(t.title, Modifier.weight(1f))
                        Text(
                            "КД ${t.ac} · ${t.hp}/${t.maxHp}" + if (attacker != null) " · ${feet(project, attacker, t)} фт" else "",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                AdvantageRow(advantage) { advantage = it }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(apply, { apply = it })
                    Text("Сразу снять хиты с цели")
                }
                result?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = attacker != null && targetId != null && attacks.isNotEmpty(),
                onClick = {
                    val a = attacks.getOrNull(attackIndex) ?: return@TextButton
                    result = viewModel.attack(attackerId!!, a, targetId!!, advantage, apply)?.text
                }
            ) { Text("🎲 Бросить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

/**
 * Проверка: герой бросает навык, всё спрятанное со сложностью не выше
 * результата отмечается найденным — мастер открывает его игрокам.
 */
@Composable
fun CheckDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
    val heroes = project.tokens.filter { it.faction == TokenFaction.HERO || it.faction == TokenFaction.ALLY }
    val start = (viewModel.selection as? Selection.TokenSel)?.id ?: heroes.firstOrNull()?.id
    var tokenId by remember { mutableStateOf(start) }
    var skill by remember { mutableStateOf(CheckSkill.PERCEPTION) }
    var save by remember { mutableStateOf<Ability?>(null) }
    var advantage by remember { mutableIntStateOf(0) }
    var total by remember { mutableStateOf<Int?>(null) }
    var text by remember { mutableStateOf<String?>(null) }
    var picked by remember { mutableStateOf(setOf<String>()) }
    val hidden = viewModel.hiddenThings()
    val token = project.tokens.firstOrNull { it.id == tokenId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Проверка") },
        text = {
            Column(Modifier.heightIn(max = 540.dp).verticalScroll(rememberScrollState())) {
                Text("Кто бросает", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (t in project.tokens.filter { !it.dead }) {
                        FilterChip(t.id == tokenId, { tokenId = t.id }, label = { Text(t.title) })
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Навык", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (s in CheckSkill.entries) {
                        val bonus = token?.let { Dnd.sheetOf(it).skillBonus(s) }
                        FilterChip(save == null && skill == s, { skill = s; save = null }, label = {
                            Text(s.title + if (bonus != null && s != CheckSkill.ANY) " ${Dnd.signed(bonus)}" else "")
                        })
                    }
                }
                Text("или спасбросок", style = MaterialTheme.typography.labelSmall)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (a in Ability.entries) {
                        FilterChip(save == a, { save = a }, label = { Text(a.short) })
                    }
                }
                Spacer(Modifier.height(6.dp))
                AdvantageRow(advantage) { advantage = it }
                text?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, fontWeight = FontWeight.Bold)
                }
                if (hidden.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Спрятано на карте", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Отмечено то, что найдено этим броском (КС не выше результата и навык подходит). " +
                            "Можно отметить и открыть что угодно своим решением.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    for (thing in hidden) {
                        val near = token?.let { kotlin.math.round(it.pos.distanceTo(thing.pos) / project.gridCell).toInt() * project.feetPerCell }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(thing.ref.id in picked, { on ->
                                picked = if (on) picked + thing.ref.id else picked - thing.ref.id
                            })
                            Column(Modifier.weight(1f)) {
                                Text(thing.title)
                                Text(
                                    "КС ${thing.secret.dc} · ${thing.secret.skill.title}" + if (near != null) " · $near фт" else "",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                    TextButton(
                        enabled = picked.isNotEmpty(),
                        onClick = {
                            viewModel.reveal(hidden.filter { it.ref.id in picked }.map { it.ref })
                            picked = emptySet()
                        }
                    ) { Text("✨ Открыть отмеченное игрокам") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val r = viewModel.rollCheck(tokenId, skill, save, advantage, 0)
                total = r.total
                text = r.text
                if (save == null) {
                    picked = hidden.filter {
                        it.secret.dc <= r.total &&
                            (it.secret.skill == skill || it.secret.skill == CheckSkill.ANY || skill == CheckSkill.ANY)
                    }.map { it.ref.id }.toSet()
                }
            }) { Text("🎲 Бросить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } }
    )
}

@Composable
fun LogDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val log = viewModel.project?.scene?.log.orEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Журнал игры") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                if (log.isEmpty()) Text("Пока пусто: здесь появятся броски, атаки и находки.")
                for (line in log) {
                    Text(line, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 3.dp))
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Закрыть") } },
        dismissButton = { TextButton(onClick = { viewModel.clearLog() }) { Text("Очистить") } }
    )
}

/** Спрятать объект, стену, зону или фишку и задать, какой проверкой их находят. */
@Composable
fun SecretDialog(viewModel: EditorViewModel, target: Selection, onDismiss: () -> Unit) {
    val initial = viewModel.secretOf(target) ?: Secret()
    var hidden by remember { mutableStateOf(true) }
    var dc by remember { mutableFloatStateOf(initial.dc.toFloat()) }
    var skill by remember { mutableStateOf(initial.skill) }
    var reveal by remember { mutableStateOf(initial.reveal) }
    val isToken = target is Selection.TokenSel
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Тайник") },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState())) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Спрятано от игроков", Modifier.weight(1f))
                    Switch(hidden, { hidden = it })
                }
                Text("Сложность (КС): ${dc.toInt()}", style = MaterialTheme.typography.labelLarge)
                Slider(dc, { dc = it }, valueRange = 5f..30f, steps = 24)
                Text(
                    "5 — очень легко, 10 — легко, 15 — средне, 20 — трудно, 25 — очень трудно",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isToken) {
                    Spacer(Modifier.height(6.dp))
                    Text("Какой проверкой находится", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (s in CheckSkill.entries) FilterChip(skill == s, { skill = s }, label = { Text(s.title) })
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = reveal,
                        onValueChange = { reveal = it },
                        label = { Text("Что узнают игроки") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Фишку замечают Внимательностью против её Скрытности.", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.setSecret(target, Secret(hidden, dc.toInt(), skill, reveal.trim()))
                onDismiss()
            }) { Text("Готово") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}

@Composable
private fun NumberField(label: String, value: Int, modifier: Modifier = Modifier, onChange: (Int) -> Unit) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            it.toIntOrNull()?.let(onChange)
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

/** Лист персонажа: характеристики, навыки, спасброски, атаки. */
@Composable
fun SheetDialog(viewModel: EditorViewModel, onDismiss: () -> Unit) {
    val project = viewModel.project ?: return
    val tokenId = (viewModel.selection as? Selection.TokenSel)?.id ?: return
    val token = project.tokens.firstOrNull { it.id == tokenId } ?: return
    var sheet by remember { mutableStateOf(Dnd.sheetOf(token)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Лист: ${token.title}") },
        text = {
            Column(Modifier.heightIn(max = 560.dp).verticalScroll(rememberScrollState())) {
                for (row in Ability.entries.chunked(3)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (a in row) {
                            NumberField("${a.short} ${Dnd.signed(sheet.mod(a))}", sheet.score(a), Modifier.weight(1f)) { v ->
                                sheet = when (a) {
                                    Ability.STR -> sheet.copy(str = v)
                                    Ability.DEX -> sheet.copy(dex = v)
                                    Ability.CON -> sheet.copy(con = v)
                                    Ability.INT -> sheet.copy(int = v)
                                    Ability.WIS -> sheet.copy(wis = v)
                                    Ability.CHA -> sheet.copy(cha = v)
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField("Мастерство", sheet.proficiency, Modifier.weight(1f)) { sheet = sheet.copy(proficiency = it) }
                    NumberField("Скорость, фт", sheet.speed, Modifier.weight(1f)) { sheet = sheet.copy(speed = it) }
                    NumberField("Зрение, фт", sheet.vision, Modifier.weight(1f)) { sheet = sheet.copy(vision = it) }
                }
                Spacer(Modifier.height(6.dp))
                Text("Владеет навыками", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (s in CheckSkill.entries.filter { it != CheckSkill.ANY }) {
                        FilterChip(s in sheet.skills, {
                            sheet = sheet.copy(skills = if (s in sheet.skills) sheet.skills - s else sheet.skills + s)
                        }, label = { Text(s.title) })
                    }
                }
                Text("Спасброски", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (a in Ability.entries) {
                        FilterChip(a in sheet.saves, {
                            sheet = sheet.copy(saves = if (a in sheet.saves) sheet.saves - a else sheet.saves + a)
                        }, label = { Text(a.short) })
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Атаки", style = MaterialTheme.typography.labelLarge)
                sheet.attacks.forEachIndexed { i, a ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(a.name, { v -> sheet = sheet.copy(attacks = sheet.attacks.mapIndexed { j, x -> if (j == i) x.copy(name = v) else x }) },
                            label = { Text("Название") }, singleLine = true, modifier = Modifier.weight(1.4f))
                        NumberField("Бонус", a.bonus, Modifier.weight(0.8f)) { v ->
                            sheet = sheet.copy(attacks = sheet.attacks.mapIndexed { j, x -> if (j == i) x.copy(bonus = v) else x })
                        }
                        OutlinedTextField(a.damage, { v -> sheet = sheet.copy(attacks = sheet.attacks.mapIndexed { j, x -> if (j == i) x.copy(damage = v) else x }) },
                            label = { Text("Урон") }, singleLine = true, modifier = Modifier.weight(1f))
                        TextButton(onClick = { sheet = sheet.copy(attacks = sheet.attacks.filterIndexed { j, _ -> j != i }) }) { Text("✕") }
                    }
                }
                TextButton(onClick = { sheet = sheet.copy(attacks = sheet.attacks + Attack("Новая атака", 3, "1к6")) }) { Text("+ Атака") }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.updateSheet(token.id, sheet)
                onDismiss()
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}
