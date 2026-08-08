package com.turkce.kelimesolitaire.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.turkce.kelimesolitaire.R

@Composable
fun CoinIcon(
    size: Dp = 24.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.coin),
        contentDescription = "Coin",
        modifier = modifier.size(size)
    )
}
