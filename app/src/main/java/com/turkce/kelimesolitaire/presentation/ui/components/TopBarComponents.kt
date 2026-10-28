package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import com.turkce.kelimesolitaire.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.turkce.kelimesolitaire.presentation.ui.theme.CoinBoxBorder
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkMain
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper


@Composable
fun CoinCounter(
    coins: Int,
    isPersian: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val coinScale = remember { Animatable(1f) }
    val nunitoFont = rememberNunitoFont()
    Box(
        modifier = Modifier
            .scale(coinScale.value)
            .clickable(enabled = onClick != null) {
                onClick?.invoke() // اجرا کردن کلیک ورودی
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Box border extending rightward from behind the coin (transparent background)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(start = 18.dp)
                .height(30.dp)
                .border(
                    1.5.dp,
                    CoinBoxBorder,
                    RoundedCornerShape(
                        topEnd = 14.dp,
                        bottomEnd = 14.dp,
                        topStart = 4.dp,
                        bottomStart = 4.dp
                    )
                )
                .padding(start = 22.dp, end = 12.dp)
        ) {
            Text(
                text = LocaleHelper.formatNumber(coins, isPersian),
                color = DarkMain,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = nunitoFont
            )
        }

        // 3D Coin Icon on the outside, overlapping on the left
        Box(
            modifier = Modifier
                .size(40.dp)
                .zIndex(2f),
            contentAlignment = Alignment.Center
        ) {
            com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon(size = 38.dp)
        }
    }
}


@Composable
fun TopBarIconButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ){onClick()}
    )
}

