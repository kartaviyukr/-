package com.kartaviy.tgagent.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.kartaviy.tgagent.App
import org.drinkless.tdlib.TdApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppTheme { Root() } }
    }
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colors, content = content)
}

@Composable
fun Root() {
    val app = App.instance
    var showSettings by remember { mutableStateOf(!app.prefs.isConfigured) }
    val auth by app.tg.authState.collectAsState()

    if (showSettings) {
        SettingsScreen(
            canClose = app.prefs.isConfigured,
            onDone = {
                if (!app.tg.isStarted) app.tg.start()
                showSettings = false
            },
        )
        return
    }
    when (auth) {
        is TdApi.AuthorizationStateReady -> AgentScreen(onSettings = { showSettings = true })
        is TdApi.AuthorizationStateWaitPhoneNumber,
        is TdApi.AuthorizationStateWaitCode,
        is TdApi.AuthorizationStateWaitPassword,
        is TdApi.AuthorizationStateWaitEmailAddress,
        is TdApi.AuthorizationStateWaitEmailCode,
        is TdApi.AuthorizationStateWaitOtherDeviceConfirmation,
        is TdApi.AuthorizationStateWaitRegistration -> LoginScreen(auth!!, onSettings = { showSettings = true })
        else -> LoadingScreen(onSettings = { showSettings = true })
    }
}
