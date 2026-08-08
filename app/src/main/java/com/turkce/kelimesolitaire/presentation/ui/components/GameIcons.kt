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
fun HintIcon(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.hint),
        contentDescription = "Hint",
        modifier = modifier.size(size)
    )
}

@Composable
fun UndoIcon(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.undo),
        contentDescription = "Undo",
        modifier = modifier.size(size)
    )
}

@Composable
fun JokerIcon(
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.joker),
        contentDescription = "Joker",
        modifier = modifier.size(size)
    )
}

@Composable
fun AdIcon(
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.play),
        contentDescription = "Watch Ad",
        modifier = modifier.size(size)
    )
}

@Composable
fun TvIcon(
    size: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.tv),
        contentDescription = "TV Ad",
        modifier = modifier.size(size)
    )
}
