package com.turkce.kelimesolitaire.presentation.ui.screens

import android.graphics.Bitmap
import android.graphics.LinearGradient as AndroidLinearGradient
import android.graphics.Paint as AndroidPaint
import android.graphics.Shader as AndroidShader
import android.graphics.Typeface as AndroidTypeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.turkce.kelimesolitaire.R
import com.turkce.kelimesolitaire.presentation.ui.components.AdBannerPlaceholder
import com.turkce.kelimesolitaire.presentation.ui.components.CoinIcon
import com.turkce.kelimesolitaire.presentation.ui.components.ConfettiPartyPopper
import com.turkce.kelimesolitaire.presentation.ui.components.rememberNunitoFont
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkBg
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FestiveBuntingAndStar(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "starShimmer")
    val starGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starGlowScale"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
    ) {
        val width = size.width
        val centerX = width / 2f
        val starY = 46.dp.toPx()
        val starOuterR = 34.dp.toPx()
        val starInnerR = 16.dp.toPx()

        // 1. Pulsing Golden Halo behind Star
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x88F59E0B),
                    Color(0x33F59E0B),
                    Color.Transparent
                ),
                center = Offset(centerX, starY),
                radius = 65.dp.toPx() * starGlowScale
            ),
            center = Offset(centerX, starY),
            radius = 65.dp.toPx() * starGlowScale
        )

        // 2. Festive Bunting Strings & Hanging Triangular Flags
        val leftStringStart = Offset(centerX - 12.dp.toPx(), starY + 6.dp.toPx())
        val leftStringControl = Offset(centerX * 0.45f, starY + 28.dp.toPx())
        val leftStringEnd = Offset(-15.dp.toPx(), 22.dp.toPx())

        val rightStringStart = Offset(centerX + 12.dp.toPx(), starY + 6.dp.toPx())
        val rightStringControl = Offset(centerX + (width - centerX) * 0.55f, starY + 28.dp.toPx())
        val rightStringEnd = Offset(width + 15.dp.toPx(), 22.dp.toPx())

        val flagColors = listOf(
            Color(0xFFEF4444), // Red
            Color(0xFFFBBF24), // Yellow
            Color(0xFF3B82F6), // Blue
            Color(0xFFF97316), // Orange
            Color(0xFF06B6D4), // Cyan
            Color(0xFF10B981)  // Green
        )

        val flagCount = 5
        val flagDrop = 22.dp.toPx()
        val flagOutlineColor = Color(0xFF78350F).copy(alpha = 0.5f)
        val outlineStroke = Stroke(width = 1.5.dp.toPx())

        fun getQuadPoint(p0: Offset, p1: Offset, p2: Offset, t: Float): Offset {
            val u = 1f - t
            val x = u * u * p0.x + 2 * u * t * p1.x + t * t * p2.x
            val y = u * u * p0.y + 2 * u * t * p1.y + t * t * p2.y
            return Offset(x, y)
        }

        // Draw Left Flags (outwards from center)
        for (i in 0 until flagCount) {
            val t1 = (i.toFloat() / flagCount) * 0.95f
            val t2 = ((i + 1).toFloat() / flagCount) * 0.95f
            val pt1 = getQuadPoint(leftStringStart, leftStringControl, leftStringEnd, t1)
            val pt2 = getQuadPoint(leftStringStart, leftStringControl, leftStringEnd, t2)
            val midX = (pt1.x + pt2.x) / 2f
            val midY = (pt1.y + pt2.y) / 2f + flagDrop

            val flagPath = Path().apply {
                moveTo(pt1.x, pt1.y)
                lineTo(pt2.x, pt2.y)
                lineTo(midX, midY)
                close()
            }
            val color = flagColors[i % flagColors.size]
            drawPath(flagPath, color)
            drawPath(flagPath, flagOutlineColor, style = outlineStroke)
        }

        // Draw Right Flags (outwards from center)
        for (i in 0 until flagCount) {
            val t1 = (i.toFloat() / flagCount) * 0.95f
            val t2 = ((i + 1).toFloat() / flagCount) * 0.95f
            val pt1 = getQuadPoint(rightStringStart, rightStringControl, rightStringEnd, t1)
            val pt2 = getQuadPoint(rightStringStart, rightStringControl, rightStringEnd, t2)
            val midX = (pt1.x + pt2.x) / 2f
            val midY = (pt1.y + pt2.y) / 2f + flagDrop

            val flagPath = Path().apply {
                moveTo(pt1.x, pt1.y)
                lineTo(pt2.x, pt2.y)
                lineTo(midX, midY)
                close()
            }
            val color = flagColors[(flagColors.size - 1 - i) % flagColors.size]
            drawPath(flagPath, color)
            drawPath(flagPath, flagOutlineColor, style = outlineStroke)
        }

        // Draw Strings over flags
        val leftString = Path().apply {
            moveTo(leftStringStart.x, leftStringStart.y)
            quadraticBezierTo(leftStringControl.x, leftStringControl.y, leftStringEnd.x, leftStringEnd.y)
        }
        val rightString = Path().apply {
            moveTo(rightStringStart.x, rightStringStart.y)
            quadraticBezierTo(rightStringControl.x, rightStringControl.y, rightStringEnd.x, rightStringEnd.y)
        }
        val stringStroke = Stroke(width = 2.5.dp.toPx())
        val stringColor = Color(0xFF78350F)
        drawPath(leftString, stringColor, style = stringStroke)
        drawPath(rightString, stringColor, style = stringStroke)

        // 3. 3D Faceted Golden Star at Top Center
        val starPoints = mutableListOf<Offset>()
        for (k in 0 until 10) {
            val r = if (k % 2 == 0) starOuterR else starInnerR
            val angleRad = Math.toRadians((k * 36 - 90).toDouble())
            val px = (centerX + r * Math.cos(angleRad)).toFloat()
            val py = (starY + r * Math.sin(angleRad)).toFloat()
            starPoints.add(Offset(px, py))
        }

        // Base 3D Bottom Bevel Shadow
        val shadowStar = Path().apply {
            moveTo(starPoints[0].x, starPoints[0].y + 4.dp.toPx())
            for (k in 1 until 10) {
                lineTo(starPoints[k].x, starPoints[k].y + 4.dp.toPx())
            }
            close()
        }
        drawPath(shadowStar, Color(0xFF78350F))

        // Main Star Path
        val mainStar = Path().apply {
            moveTo(starPoints[0].x, starPoints[0].y)
            for (k in 1 until 10) {
                lineTo(starPoints[k].x, starPoints[k].y)
            }
            close()
        }

        // Thick Star Outline
        drawPath(
            mainStar,
            Color(0xFF78350F),
            style = Stroke(width = 4.dp.toPx())
        )

        // Star Body Fill
        drawPath(
            mainStar,
            Brush.verticalGradient(
                listOf(
                    Color(0xFFFFFBEB), // Glossy top
                    Color(0xFFFDE047), // Vivid gold
                    Color(0xFFF59E0B), // Warm amber
                    Color(0xFFD97706)  // Rich depth
                ),
                startY = starY - starOuterR,
                endY = starY + starOuterR
            )
        )

        // Star 3D Faceted Shading (Alternating Sheen & Shade)
        val centerPt = Offset(centerX, starY)
        for (k in 0 until 10) {
            val ptNext = starPoints[(k + 1) % 10]
            val facet = Path().apply {
                moveTo(centerPt.x, centerPt.y)
                lineTo(starPoints[k].x, starPoints[k].y)
                lineTo(ptNext.x, ptNext.y)
                close()
            }
            val facetColor = if (k % 2 == 0) {
                Color.White.copy(alpha = 0.28f)
            } else {
                Color(0xFF78350F).copy(alpha = 0.16f)
            }
            drawPath(facet, facetColor)
        }
    }
}

@Composable
fun CurvedVictoryTitle(
    text: String,
    isPersian: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val bitmapWidth = with(density) { 340.dp.roundToPx() }
    val bitmapHeight = with(density) { 85.dp.roundToPx() }

    val fontResId = if (isPersian) R.font.lalezar else R.font.nunito_black
    val typeface = remember(context, isPersian) {
        ResourcesCompat.getFont(context, fontResId) ?: AndroidTypeface.DEFAULT_BOLD
    }

    val cleanText = remember(text) {
        text.replace("\u200F", "").trim()
    }

    val textBitmap = remember(cleanText, typeface, bitmapWidth, bitmapHeight) {
        val bitmap = Bitmap.createBitmap(
            bitmapWidth,
            bitmapHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)

        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            this.typeface = typeface
            this.textSize = bitmapHeight * 0.65f
            this.textAlign = AndroidPaint.Align.CENTER
        }

        val x = bitmapWidth / 2f
        val fontMetrics = paint.fontMetrics
        val y = (bitmapHeight / 2f) - (fontMetrics.ascent + fontMetrics.descent) / 2f

        // 1. 3D Bottom Bevel Shadow
        paint.style = AndroidPaint.Style.STROKE
        paint.strokeWidth = 14f
        paint.strokeJoin = AndroidPaint.Join.ROUND
        paint.strokeCap = AndroidPaint.Cap.ROUND
        paint.color = android.graphics.Color.parseColor("#78350F")
        canvas.drawText(cleanText, x, y + 5f, paint)

        // 2. Thick Outer Border
        paint.color = android.graphics.Color.parseColor("#451A03")
        canvas.drawText(cleanText, x, y, paint)

        // 3. Inner Face with Golden Yellow Gradient
        paint.style = AndroidPaint.Style.FILL
        paint.shader = AndroidLinearGradient(
            0f, y + fontMetrics.ascent,
            0f, y + fontMetrics.descent,
            intArrayOf(
                android.graphics.Color.parseColor("#FFFBEB"), // Top bright highlight
                android.graphics.Color.parseColor("#FDE047"), // Golden yellow
                android.graphics.Color.parseColor("#F59E0B")  // Deep amber
            ),
            floatArrayOf(0f, 0.45f, 1f),
            AndroidShader.TileMode.CLAMP
        )
        canvas.drawText(cleanText, x, y, paint)

        bitmap
    }

    // Mesh deformation for smooth convex arch (Curved Text)
    val meshWidth = 24
    val meshHeight = 4
    val verts = remember(bitmapWidth, bitmapHeight) {
        val vertCount = (meshWidth + 1) * (meshHeight + 1)
        val vArray = FloatArray(vertCount * 2)
        val arcHeight = bitmapHeight * 0.28f
        var index = 0
        for (r in 0..meshHeight) {
            val v = r.toFloat() / meshHeight
            for (c in 0..meshWidth) {
                val u = c.toFloat() / meshWidth
                val x = u * bitmapWidth
                val archOffset = -arcHeight * kotlin.math.sin(u * Math.PI).toFloat()
                val y = v * bitmapHeight + archOffset + arcHeight * 0.85f
                vArray[index * 2] = x
                vArray[index * 2 + 1] = y
                index++
            }
        }
        vArray
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp)
    ) {
        val canvasWidth = size.width
        val scale = (canvasWidth * 0.94f) / bitmapWidth

        drawIntoCanvas { canvas ->
            canvas.save()
            val translateX = (canvasWidth - bitmapWidth * scale) / 2f
            canvas.translate(translateX, 0f)
            canvas.scale(scale, scale)

            canvas.nativeCanvas.drawBitmapMesh(
                textBitmap,
                meshWidth,
                meshHeight,
                verts,
                0,
                null,
                0,
                null
            )
            canvas.restore()
        }
    }
}

@Composable
fun LevelCompleteScreen(
    levelNumber: Int,
    bonusCoins: Int,
    isRewardDoubled: Boolean = false,
    onDoubleRewardClicked: () -> Unit = {},
    onNormalRewardClicked: () -> Unit = {},
    onNextLevelClicked: () -> Unit = {},
    onMainMenuClicked: () -> Unit = {},
    isAdFree: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val nunitoFont = rememberNunitoFont()
    var isActionTriggered by remember { mutableStateOf(false) }

    // Auto-reset action debouncing in case rewarded ad dismisses or fails to launch
    LaunchedEffect(isActionTriggered) {
        if (isActionTriggered) {
            delay(4000L)
            isActionTriggered = false
        }
    }

    // Effective coin rewards
    val effectiveBonus = if (bonusCoins > 0) bonusCoins else 20
    val effectiveDoubleBonus = effectiveBonus * 2

    // Celebratory victory pop entrance animation
    val entranceScale = remember { Animatable(0.2f) }
    val entranceAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            entranceAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(350, easing = LinearOutSlowInEasing)
            )
        }
        launch {
            entranceScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    // Dynamic attention-grabbing shake & wobble animation for the 2X Double Reward Button
    val infiniteTransition = rememberInfiniteTransition(label = "doubleButtonShakeTransition")

    val shakeRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2200
                0f at 0
                0f at 1200
                -3.5f at 1300
                3.5f at 1400
                -3.5f at 1500
                3f at 1600
                -2f at 1700
                1.5f at 1800
                0f at 1900
                0f at 2200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "shakeRotation"
    )

    val shakeOffsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2200
                0f at 0
                0f at 1200
                -6f at 1300
                6f at 1400
                -5f at 1500
                4f at 1600
                -3f at 1700
                2f at 1800
                0f at 1900
                0f at 2200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "shakeOffsetX"
    )

    val shakeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2200
                1f at 0
                1f at 1200
                1.04f at 1400
                1.04f at 1650
                1f at 1900
                1f at 2200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "shakeScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            // Celebratory Victory Header (Bunting, 3D Golden Star, Curved Cartoon Victory Title)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = entranceScale.value
                        scaleY = entranceScale.value
                        alpha = entranceAlpha.value
                    }
            ) {
                FestiveBuntingAndStar()
                CurvedVictoryTitle(
                    text = LocaleHelper.victoryTitle(isPersian),
                    isPersian = isPersian,
                    modifier = Modifier.offset(y = (-14).dp)
                )
            }

            // Reward Action Buttons (Only 2 buttons: Double Reward with Video badge & Shake, and Normal Reward)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Double Reward Button (2X) with Shaking Animation & Video Icon Badge on Edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.50f)
                        .graphicsLayer {
                            rotationZ = shakeRotation
                            translationX = shakeOffsetX
                            scaleX = shakeScale
                            scaleY = shakeScale
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    // 3D Tactile Golden Amber Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFDE047),
                                        Color(0xFFF59E0B),
                                        Color(0xFFD97706)
                                    )
                                )
                            )
                            .border(1.8.dp, Color(0xFFFEF08A), RoundedCornerShape(22.dp))
                            .padding(3.5.dp)
                            .clip(RoundedCornerShape(18.5.dp))
                            .background(Color(0xFF78350F)) // 3D bevel shadow base
                            .padding(bottom = 5.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFFEF08A),
                                        Color(0xFFFBBF24),
                                        Color(0xFFF59E0B)
                                    )
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(15.dp))
                            .clickable(
                                enabled = !isActionTriggered,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!isActionTriggered) {
                                    isActionTriggered = true
                                    GameSettingsManager.playButtonClickSound(context)
                                    onDoubleRewardClicked()
                                }
                            }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isPersian) {
                                Text(
                                    text = LocaleHelper.formatNumber(effectiveDoubleBonus, isPersian),
                                    color = Color(0xFF451A03),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                CoinIcon(size = 46.dp)
                            } else {
                                CoinIcon(size = 46.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = LocaleHelper.formatNumber(effectiveDoubleBonus, isPersian),
                                    color = Color(0xFF451A03),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = nunitoFont,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Video Ad Icon Badge overlapping on the edge (Enlarged)
                    Box(
                        modifier = Modifier
                            .align(if (isPersian) Alignment.TopStart else Alignment.TopEnd)
                            .offset(
                                x = if (isPersian) 10.dp else (-10).dp,
                                y = (-16).dp
                            )
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFFF3366),
                                        Color(0xFFE11D48),
                                        Color(0xFFBE123C)
                                    )
                                )
                            )
                            .border(2.dp, Color.White, CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.tv),
                            contentDescription = "Video Ad",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Normal Reward Button (1X) - Identical dimensions to Double button
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.50f)
                        .shadow(12.dp, RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF64748B),
                                    Color(0xFF475569)
                                )
                            )
                        )
                        .border(1.8.dp, Color(0xFF94A3B8).copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                        .padding(3.5.dp)
                        .clip(RoundedCornerShape(18.5.dp))
                        .background(Color(0xFF0F172A)) // 3D dark slate bottom base
                        .padding(bottom = 5.dp) // Creates 3D bevel
                        .clip(RoundedCornerShape(15.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF475569),
                                    Color(0xFF334155),
                                    Color(0xFF1E293B)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF64748B).copy(alpha = 0.4f), RoundedCornerShape(15.dp))
                        .clickable(
                            enabled = !isActionTriggered,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isActionTriggered) {
                                isActionTriggered = true
                                GameSettingsManager.playButtonClickSound(context)
                                val action = if (onNormalRewardClicked != {}) onNormalRewardClicked else onMainMenuClicked
                                action()
                            }
                        }
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isPersian) {
                            Text(
                                text = LocaleHelper.formatNumber(effectiveBonus, isPersian),
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont,
                                maxLines = 1,
                                softWrap = false
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            CoinIcon(size = 46.dp)
                        } else {
                            CoinIcon(size = 46.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LocaleHelper.formatNumber(effectiveBonus, isPersian),
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = nunitoFont,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Ad Banner Footer
            AdBannerPlaceholder(isAdFree = isAdFree)
        }

        // Celebratory Party Popper Confetti Shower
        ConfettiPartyPopper()
    }
}
