package ru.velopodmoskovye.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.velopodmoskovye.domain.model.Title
import ru.velopodmoskovye.ui.common.TitleAwardDialog
import ru.velopodmoskovye.ui.place.StopRow
import ru.velopodmoskovye.ui.theme.VeloTheme

@RunWith(AndroidJUnit4::class)
class ChecklistAndAwardTest {
    @get:Rule
    val compose = createComposeRule()

    private val title = Title("t", "Звонарь Реута", "Легенда о колоколе", "Позвонить в колокол", "Звонари Реута", null)

    @Test
    fun checklistRowShowsStopAndReactsToClick() {
        var clicked = false
        compose.setContent {
            VeloTheme {
                StopRow(1, "Колокол «Реут»", "смотреть снаружи", optional = false, visited = false, eta = null, onClick = { clicked = true })
            }
        }
        compose.onNodeWithText("1. Колокол «Реут»").assertIsDisplayed().performClick()
        assertTrue(clicked)
    }

    @Test
    fun awardDialogAppearsWhenLastStopIsChecked() {
        compose.setContent {
            VeloTheme {
                var visited by remember { mutableStateOf(setOf("a")) }
                Column {
                    StopRow(1, "Точка А", "смотреть снаружи", false, "a" in visited, null) {}
                    StopRow(2, "Точка Б", "смотреть снаружи", false, "b" in visited, null) {}
                    Button(onClick = { visited = visited + "b" }) { Text("Отметить Б") }
                }
                if (visited.containsAll(listOf("a", "b"))) {
                    TitleAwardDialog(AwardUi("place:reutov", title.name, title.legend, title), pairMode = false) {}
                }
            }
        }
        compose.onNodeWithTag("award_dialog").assertDoesNotExist()
        compose.onNodeWithText("Отметить Б").performClick()
        compose.onNodeWithTag("award_dialog").assertIsDisplayed()
        compose.onNodeWithText("Звонарь Реута").assertIsDisplayed()
        compose.onNodeWithText("Позвонить в колокол").assertIsDisplayed()
    }

    @Test
    fun awardDialogShowsPairVariant() {
        compose.setContent {
            VeloTheme { TitleAwardDialog(AwardUi("place:reutov", title.name, title.legend, title), pairMode = true) {} }
        }
        compose.onNodeWithText("Звонари Реута").assertIsDisplayed()
    }
}
