package ru.velopodmoskovye.ui.common

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import ru.velopodmoskovye.ui.AwardUi
import ru.velopodmoskovye.ui.theme.StarGold
import java.io.File
import java.io.FileOutputStream

@Composable
fun TitleAwardDialog(award: AwardUi, pairMode: Boolean, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scale = remember(award.id) { Animatable(0.2f) }
    val rotation = remember(award.id) { Animatable(-160f) }
    val alpha = remember(award.id) { Animatable(0f) }
    LaunchedEffect(award.id) {
        launch { alpha.animateTo(1f, tween(500)) }
        launch { rotation.animateTo(0f, tween(900, easing = FastOutSlowInEasing)) }
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    val title = award.placeTitle
    val shownName = if (pairMode && title != null && award.id.startsWith("place:")) title.pairVariant else award.name

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("award_dialog"),
        icon = {
            Icon(
                Icons.Filled.EmojiEvents, null, tint = StarGold,
                modifier = Modifier.size(72.dp).scale(scale.value).rotate(rotation.value),
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.alpha(alpha.value)) {
                Text("Новое звание", style = MaterialTheme.typography.labelLarge)
                Text(shownName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(
                Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()).alpha(alpha.value),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (title != null && award.id.startsWith("place:")) {
                    Text("Легенда", fontWeight = FontWeight.SemiBold)
                    Text(title.legend)
                    Text("Ритуал", fontWeight = FontWeight.SemiBold)
                    Text(title.ritual)
                    if (!pairMode) Text("Для двоих: «${title.pairVariant}»", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(award.description)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Здорово!") } },
        dismissButton = {
            TextButton(onClick = {
                shareTitle(context, shownName, title?.legend ?: award.description, title?.ritual)
            }) { Text("Поделиться") }
        },
    )
}

/** Карточка звания картинкой — рисуем обычным Canvas, без скриншотов UI. */
fun shareTitle(context: Context, name: String, legend: String, ritual: String?) {
    val w = 1080
    val h = 1350
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val bg = Paint().apply {
        shader = LinearGradient(0f, 0f, 0f, h.toFloat(), 0xFF1F6F4A.toInt(), 0xFF0D2E20.toInt(), Shader.TileMode.CLAMP)
    }
    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)
    val gold = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF2B01E.toInt() }
    c.drawCircle(w / 2f, 230f, 110f, gold)
    val star = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF1F6F4A.toInt(); textSize = 150f; textAlign = Paint.Align.CENTER }
    c.drawText("★", w / 2f, 282f, star)

    fun block(text: String, size: Float, bold: Boolean, top: Float, color: Int = 0xFFFFFFFF.toInt()): Float {
        val p = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, p, w - 160)
            .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, 1.15f).setMaxLines(9).build()
        c.save()
        c.translate(80f, top)
        layout.draw(c)
        c.restore()
        return top + layout.height
    }
    var y = block("ВЕЛОПОДМОСКОВЬЕ · НОВОЕ ЗВАНИЕ", 36f, true, 380f, 0xFFF2B01E.toInt())
    y = block(name, 76f, true, y + 30f)
    y = block(legend, 38f, false, y + 50f, 0xFFE6F2EA.toInt())
    if (ritual != null) block("Ритуал: $ritual", 34f, false, y + 40f, 0xFFB7F0CF.toInt())

    val dir = File(context.cacheDir, "share").apply { mkdirs() }
    val file = File(dir, "title.png")
    FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Моё звание в «Велоподмосковье»: $name")
        clipData = ClipData.newRawUri(null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Поделиться званием").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
