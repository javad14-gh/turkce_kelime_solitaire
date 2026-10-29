package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.zIndex
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupBg
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupFrameBase
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupFrameGradient
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTitleOutline
import com.turkce.kelimesolitaire.presentation.ui.theme.PopupTrayDepth

/**
 * Standard popup: dim scrim + dark 3D frame + title header + light tray.
 * onDismiss == null  -> no close button, back / outside taps are ignored.
 */
@Composable
fun PopupFrame(
    title: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    titleSize: TextUnit = 26.sp,
    maxWidth: Dp = 330.dp,
    trayMaxHeight: Dp = 500.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler { onDismiss?.invoke() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(500f)
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss?.invoke() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(24.dp, RoundedCornerShape(32.dp))
                .clip(RoundedCornerShape(32.dp))
                .background(PopupFrameBase)
                .padding(bottom = 7.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.verticalGradient(PopupFrameGradient))
                // swallow taps inside the frame so they don't hit the scrim
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {}
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedText(
                        text = title,
                        textColor = Color.White,
                        outlineColor = PopupTitleOutline,
                        outlineWidth = 12.5f,
                        fontSize = titleSize,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = if (onDismiss != null) 42.dp else 0.dp)
                    )
                    if (onDismiss != null) {
                        Image(
                            painter = painterResource(id = R.drawable.cancel),
                            contentDescription = "Close",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(38.dp)
                                .clickable { onDismiss() }
                        )
                    }
                }

                // Light tray
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, bottom = 12.dp)
                        .shadow(10.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(PopupTrayDepth)
                        .padding(bottom = 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(PopupBg)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = trayMaxHeight)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 16.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        content = content
                    )
                }
            }
        }
    }
}