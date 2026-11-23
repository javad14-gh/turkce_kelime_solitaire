package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.turkce.kelimesolitaire.presentation.ui.theme.MenuBtnOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkMain

// Red "exit / give up" style shared by all popups
val MenuRedBrush: Brush = Brush.verticalGradient(
    listOf(
        Color(0xFFFF4D4D),
        Color(0xFFF30000)
    )
)
val MenuRedBack: Color = Color(0xFFC50000)
val MenuRedOutline = Color(0xFF7F1D1D)

/** Icon + label; order flipped for Persian. */
@Composable
fun IconLabelRow(
    isPersian: Boolean,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isPersian) {
            label()
            Spacer(modifier = Modifier.width(8.dp))
            icon()
        } else {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
            label()
        }
    }
}

/** White outlined label used inside candy buttons. */
@Composable
fun PopupButtonLabel(
    text: String,
    outline: Color,
    fontSize: TextUnit = 16.sp
) {
    OutlinedText(
        text = text,
        textColor = Color.White,
        outlineColor = outline,
        outlineWidth = 6f,
        fontSize = fontSize,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center,
        shadowColor = outline.copy(alpha = 0.6f),
        shadowOffsetY = 3f
    )
}

/** Wide menu button (theme / store / restart / exit ...). */
@Composable
fun MenuWideButton(
    text: String,
    isPersian: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    back: Color = DarkMain,
    face: Brush = Brush.verticalGradient(listOf(Color(0xFFFFF6E6), Color(0xFFEADFC8))),
    maxWidth: Dp = 280.dp,
    outline: Color = MenuBtnOutline,
    fontSize: TextUnit = 22.sp,
    icon: @Composable () -> Unit
) {
    val outerShape = RoundedCornerShape(24.dp)
    val innerShape = RoundedCornerShape(19.dp)
    Box(
        modifier = modifier
            .widthIn(max = maxWidth)
            .fillMaxWidth(0.82f)
            .heightIn(min = 84.dp)
            .shadow(10.dp, outerShape)
            .clip(outerShape)
            .background(back)
            .candyGloss(corner = 24.dp, inset = 5.dp)
            .padding(8.dp)
            .clip(innerShape)
            .background(face)
            .clickable(onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        IconLabelRow(
            isPersian = isPersian,
            icon = icon,
            label = {
                OutlinedText(
                    text = text,
                    textColor = Color.White,
                    outlineColor = outline,
                    outlineWidth = 12.5f,
                    shadowColor = outline.copy(alpha = 0.6f),
                    shadowOffsetY = 3f,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black
                )
            }
        )
    }
}

/** Standard 57dp drawable icon for MenuWideButton. */
@Composable
fun MenuIcon(res: Int, description: String) {
    Image(
        painter = painterResource(id = res),
        contentDescription = description,
        modifier = Modifier.size(50.dp)
    )
}