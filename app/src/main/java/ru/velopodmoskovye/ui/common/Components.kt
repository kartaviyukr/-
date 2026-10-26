package ru.velopodmoskovye.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.velopodmoskovye.domain.model.Fact
import ru.velopodmoskovye.domain.model.GeoPoint
import ru.velopodmoskovye.ui.theme.ProgressGreen
import ru.velopodmoskovye.ui.theme.UnverifiedAmber

fun Context.openUrl(url: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(this, "Не удалось открыть ссылку", Toast.LENGTH_SHORT).show()
    }
}

fun Context.openInMaps(point: GeoPoint, label: String) {
    val uri = "geo:${point.lat},${point.lon}?q=${point.lat},${point.lon}(${Uri.encode(label)})"
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        openUrl("https://www.google.com/maps/search/?api=1&query=${point.lat},${point.lon}")
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(top = 20.dp, bottom = 8.dp),
    )
}

@Composable
fun Paragraphs(paragraphs: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        paragraphs.forEach { Text(it, style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("$label: ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun VerifiedMark(verified: Boolean) {
    if (verified) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Verified, null, tint = ProgressGreen, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("проверено", style = MaterialTheme.typography.labelSmall, color = ProgressGreen)
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.HelpOutline, null, tint = UnverifiedAmber, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("не проверено", style = MaterialTheme.typography.labelSmall, color = UnverifiedAmber)
        }
    }
}

@Composable
fun FactItem(fact: Fact) {
    val context = LocalContext.current
    Card(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(fact.text, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.padding(top = 4.dp))
                VerifiedMark(fact.verified)
            }
            IconButton(onClick = { context.openUrl(fact.sourceUrl) }) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Источник")
            }
        }
    }
}

@Composable
fun WarningCard(text: String) {
    Card(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(8.dp))
            Text(text, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
