package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper

@Composable
fun SettingsDialog(
    isPersian: Boolean,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onSoundChange: (Boolean) -> Unit,
    onHapticChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenStore: () -> Unit,
    onRestart: (() -> Unit)? = null,
    onMainMenu: (() -> Unit)? = null,
    onPrivacy: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val greyscale = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

    PopupFrame(
        title = LocaleHelper.settingsTitle(isPersian),
        onDismiss = onDismiss,
        titleSize = 32.sp,
        trayMaxHeight = 490.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.72f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SquareMenuButton(
                onClick = {
                    val next = !soundEnabled
                    onSoundChange(next)
                    if (next) GameSettingsManager.playButtonClickSound(context)
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sound),
                    contentDescription = "Sound",
                    modifier = Modifier.size(60.dp),
                    colorFilter = if (!soundEnabled) greyscale else null,
                    alpha = if (soundEnabled) 1f else 0.4f
                )
            }
            SquareMenuButton(
                onClick = {
                    val next = !hapticEnabled
                    onHapticChange(next)
                    if (next) GameSettingsManager.vibrateLight(context)
                }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vibrate),
                    contentDescription = "Vibration",
                    modifier = Modifier.size(60.dp),
                    colorFilter = if (!hapticEnabled) greyscale else null,
                    alpha = if (hapticEnabled) 1f else 0.4f
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuWideButton(
                text = if (isPersian) "رنگ‌بندی بازی" else "Oyun Teması",
                isPersian = isPersian,
                onClick = onOpenTheme,
                icon = { Text(text = "🎨", fontSize = 45.sp) }
            )

            MenuWideButton(
                text = LocaleHelper.storeTitle(isPersian),
                isPersian = isPersian,
                onClick = onOpenStore,
                icon = { MenuIcon(R.drawable.buy, "Store") }
            )

            if (onRestart != null) {
                MenuWideButton(
                    text = if (isPersian) "شروع مجدد" else "Tekrar",
                    isPersian = isPersian,
                    onClick = onRestart,
                    icon = { MenuIcon(R.drawable.restart, "Restart") }
                )
            }

            if (onPrivacy != null) {
                MenuWideButton(
                    text = LocaleHelper.privacyPolicy(isPersian),
                    isPersian = isPersian,
                    onClick = onPrivacy,
                    icon = { MenuIcon(R.drawable.shield, "Privacy Policy") }
                )
            }

            if (onMainMenu != null) {
                MenuWideButton(
                    text = LocaleHelper.mainMenu(isPersian),
                    isPersian = isPersian,
                    onClick = onMainMenu,
                    face = MenuRedBrush,
                    outline = MenuRedOutline,
                    icon = { MenuIcon(R.drawable.exit, "Main Menu") }
                )
            }
        }
    }
}

@Composable
fun ThemeDialog(
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onThemeSelected: () -> Unit
) {
    PopupFrame(
        title = if (isPersian) "انتخاب رنگ‌بندی بازی" else "Tema Seçimi",
        onDismiss = onDismiss,
        titleSize = if (isPersian) 24.sp else 28.sp
    ) {
        ThemeSelectorSection(
            isPersian = isPersian,
            fontFamily = rememberNunitoFont(),
            onThemeSelected = onThemeSelected
        )
    }
}