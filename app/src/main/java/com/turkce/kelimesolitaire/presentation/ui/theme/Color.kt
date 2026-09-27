package com.turkce.kelimesolitaire.presentation.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.turkce.kelimesolitaire.presentation.util.GameSettingsManager

// =====================================================================
// 🎨 پالت رنگ‌های جامع بازی (Game Color Palette)
// تمام رنگ‌های بازی در این فایل قرار دارند و به صورت تم‌های هماهنگ ۴ رنگی
// مدیریت می‌شوند.
// =====================================================================

// =====================================================================
// 🛠️ توابع کمکی برای تولید خودکار طیف‌های سه‌بعدی و سایه‌ها
// =====================================================================
fun Color.lighten(factor: Float): Color = Color(
    red = (red + (1f - red) * factor).coerceIn(0f, 1f),
    green = (green + (1f - green) * factor).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
    alpha = alpha
)

fun Color.darken(factor: Float): Color = Color(
    red = (red * (1f - factor)).coerceIn(0f, 1f),
    green = (green * (1f - factor)).coerceIn(0f, 1f),
    blue = (blue * (1f - factor)).coerceIn(0f, 1f),
    alpha = alpha
)

/**
 * پالت‌های تم آماده بازی با هارمونی ۴ رنگ
 */
enum class ThemePreset(
    val id: String,
    val titlePersian: String,
    val titleTurkish: String,
    val c1: Color, // ۱. بوم و میز بازی
    val c2: Color, // ۲. المان‌ها، نوشته‌ها و دکمه‌های ۳بعدی
    val c3: Color, // ۳. پشت کارت‌ها (و خط داخلی جلوی کارت)
    val c4: Color  // ۴. رنگ تاکیدی و طلایی‌ها (تاج، نوار بالا، کادر انتخابی)
) {
    CLASSIC(
        id = "classic",
        titlePersian = "کلاسیک",
        titleTurkish = "Klasik",
        c1 = Color(0xFFF0EFFB), // یاسی بسیار ملایم
        c2 = Color(0xFF221A6C), // سرمه‌ای سلطنتی
        c3 = Color(0xFF6F1594), // بنفش شاهانه
        c4 = Color(0xFFFFCB70)  // طلایی گرم
    ),
    CASINO_GREEN(
        id = "casino_green",
        titlePersian = "سبز کازینو",
        titleTurkish = "Yeşil Masa",
        c1 = Color(0xFFE8F5E9), // سبز ملایم نمدی میز
        c2 = Color(0xFF134E3F), // سبز زمردی تیره
        c3 = Color(0xFF991B1B), // قرمز زرشکی درباری کارت‌ها
        c4 = Color(0xFFF59E0B)  // طلایی کهربایی
    ),
    OCEAN_BLUE(
        id = "ocean_blue",
        titlePersian = "اقیانوس",
        titleTurkish = "Okyanus",
        c1 = Color(0xFFE0F2FE), // آبی آسمانی ملایم
        c2 = Color(0xFF0C4A6E), // آبی نفتی عمیق
        c3 = Color(0xFF0284C7), // آبی لاجوردی براق
        c4 = Color(0xFFF59E0B)  // طلایی آفتابی
    ),
    WARM_AMBER(
        id = "warm_amber",
        titlePersian = "چوبی گرم",
        titleTurkish = "Sıcak Ahşap",
        c1 = Color(0xFFFDF4E3), // کرم چوبی دنج
        c2 = Color(0xFF5D2E16), // قهوه‌ای چرم شیک
        c3 = Color(0xFFC05621), // تراکوتا / خرمایی
        c4 = Color(0xFFD97706)  // طلایی کهربایی گرم
    )
}

/**
 * مدیر وضعیت تم بازی - تغییر آنی بدون نیاز به ری‌استارت
 */
object GameThemeManager {
    var currentTheme by mutableStateOf(ThemePreset.CLASSIC)
        private set

    fun setTheme(theme: ThemePreset, context: Context? = null) {
        currentTheme = theme
        if (context != null) {
            GameSettingsManager.setSelectedTheme(context, theme.id)
        }
    }

    fun init(context: Context) {
        val savedThemeId = GameSettingsManager.getSelectedTheme(context)
        val theme = ThemePreset.values().find { it.id == savedThemeId } ?: ThemePreset.CLASSIC
        currentTheme = theme
    }
}

// ۴ متغیر اصلی که از تم انتخابی تغذیه می‌شوند:
val maincolor1: Color get() = GameThemeManager.currentTheme.c1
val maincolor2: Color get() = GameThemeManager.currentTheme.c2
val maincolor3: Color get() = GameThemeManager.currentTheme.c3
val maincolor4: Color get() = GameThemeManager.currentTheme.c4


// --- ۱. پس‌زمینه‌های اصلی بازی ---
val DarkBg = Color(0xFF131B0E)            // پس‌زمینه منوی اصلی و لودینگ بازی (سرمه‌ای کهکشانی تیره)
val GameTableBg: Color get() = maincolor1       // رنگ پس‌زمینه میز بازی (کاملاً یکدست و ساده)
val GameTableBgTop: Color get() = GameTableBg          // جهت سازگاری
val GameTableBgBottom: Color get() = GameTableBg       // جهت سازگاری

// --- هدر بالای صفحه بازی (سکه و عنوان مرحله) ---
val LevelTitleText = Color(0xFFFFFFFF)            // رنگ نوشته مرحله (مثلاً مرحله ۱)
val LevelTitleOutline: Color get() = maincolor2         // رنگ خط دور نوشته مرحله (قابل تنظیم به هر رنگ دلخواه)
val CoinBoxBorder: Color get() = maincolor4             // رنگ خط دور باکس سکه (هماهنگ با رنگ تاکیدی/طلایی)
val CoinTextColor: Color get() = maincolor2             // رنگ عدد سکه‌ها در بالای صفحه
val CoinTextOutline = Color.Transparent           // رنگ خط دور عدد سکه‌ها (اختیاری)

// --- باکس تعداد حرکات باقی‌مانده (Moves Left Card) ---
val MovesCardBg: Color get() = maincolor2          // رنگ زمینه باکس حرکات (کاملاً مستقل از بقیه بخش‌های بازی)
val MovesCardBorder: Color get() = maincolor2      // رنگ کادر دور باکس حرکات
val MovesTitleText = Color(0xFFD1D5DB)       // رنگ عنوان «حرکات باقی‌مانده»
val MovesCountText = Color(0xFFFFFFFF)       // رنگ عدد تعداد حرکات

// --- ۲. محل قرارگیری دسته‌بندی‌ها (باکس‌های بالای ستون‌ها - Category Slots) ---
// 🌟 تمام رنگ‌های زیر به صورت خودکار از روی رنگ میز (GameTableBg) محاسبه می‌شوند:
val CategorySlotBg: Color get() = GameTableBg.darken(0.07f)          // رنگ زمینه باکس خالی دسته‌بندی (کمی گودتر از میز)
val CategorySlotBgTop: Color get() = CategorySlotBg                  // جهت سازگاری
val CategorySlotBgBottom: Color get() = CategorySlotBg               // جهت سازگاری
val CategorySlotBorder: Color get() = GameTableBg.darken(0.14f)      // کادر دور باکس خالی دسته‌بندی (مرز تراشیده شده)
val CategorySlotContent: Color get() = GameTableBg.darken(0.35f)     // رنگ نوشته «کارت دسته» و آیکون تاج داخل باکس
val CategorySlotInnerShadow: Color get() = GameTableBg.darken(0.40f) // رنگ سایه داخلی باکس خالی دسته‌بندی (حس عمق و سوراخ)
val CategoryActiveBannerBg: Color get() = maincolor4          // نوار بالای کارت فعال دسته‌بندی (طلایی)
val CategorySlotActiveBg = Color(0xFFFFFFFF)            // رنگ بدنه کارت فعال در جایگاه بالا (محل نشستن کلمات مچ‌شده)
val CategorySlotMatchedBorder: Color get() = CategoryActiveBannerBg  // رنگ کادر جایگاه بالا وقتی کارت‌ها در آن قرار می‌گیرند (هماهنگ با نوار بالا)

// --- ۳. پشت کارت‌ها (Card Back) ---
val CardBackBg: Color get() = maincolor3            // رنگ ساده و یکدست پشت کارت‌ها (متصل به maincolor3)
val CardBackGradientTop: Color get() = CardBackBg          // جهت سازگاری
val CardBackGradientBottom: Color get() = CardBackBg       // جهت سازگاری
val CardBackPattern: Color get() = CardBackBg              // جهت سازگاری

// --- ۴. روی کارت‌ها (Card Front) ---
val CardFaceBg = Color(0xFFFFFFFF)            // رنگ زمینه کارت‌های کلمه و کارت‌های دسته‌بندی
val CardFaceText: Color get() = maincolor2          // رنگ متن کلمات روی کارت
val CardFaceBorder: Color get() = maincolor1.darken(0.12f)  // رنگ کادر بیرونی بسیار نازک روی کارت‌های معمولی (هماهنگ خودکار با میز)
val CardFaceInnerBorder: Color get() = CardBackBg   // رنگ کادر داخلی روی کارت‌های معمولی
val CardCategoryBorder: Color get() = maincolor4    // رنگ کادر ضخیم دور کارت دسته‌بندی (شروع از لبه، بولد و مشخص)
val CardCategoryCrown: Color get() = maincolor4     // رنگ تاج کارت دسته‌بندی
val CardCategoryText: Color get() = CardFaceText           // رنگ نوشته کارت دسته‌بندی (مثلاً پوشاک)
val CardCategoryBg: Color get() = CardFaceBg               // جهت سازگاری (زمینه مانند سایر کارت‌ها)
val CardDraggingBorder: Color get() = maincolor4.copy(alpha = 0.6f)    // رنگ کادر دور کارت در حال جابجایی (Drag) با شفافیت ملایم
val CardSelectedBorder: Color get() = maincolor4.copy(alpha = 0.7f)    // رنگ کادر دور کارت در حالت انتخاب با شفافیت ملایم
val CardJokerBgTop = Color(0xFFFEF08A)        // رنگ زمینه کارت جوکر

// --- ۵. مخزن کارت‌ها در حالت بر زدن (Stock Recycle State) ---
val StockRecycleBg: Color get() = CategorySlotBg       // رنگ پس‌زمینه مخزن خالی جهت بر زدن
val StockRecycleBorder: Color get() = CategorySlotBorder   // رنگ کادر مخزن در حالت بر زدن
val StockRecycleText: Color get() = CategorySlotContent     // رنگ متن و آیکون «بر زدن»

// --- ۶. دکمه‌های کمکی و راهنما در پایین صفحه بازی (Booster Buttons) ---
// 🌟 کافیست فقط همین یک رنگ را تغییر دهید؛ تمام طیف‌های سه‌بعدی به صورت خودکار ساخته می‌شوند:
val BoosterPrimaryColor: Color get() = maincolor2      // 🟢 رنگ اصلی دکمه‌های راهنما (کافیست فقط این رنگ را تغییر دهید)

// لایه‌های سه‌بعدی خودکار (Auto-Generated):
val BoosterFaceTop: Color get() = BoosterPrimaryColor.lighten(0.35f)           // رنگ روشن براق بالای پد دکمه
val BoosterFaceMid: Color get() = BoosterPrimaryColor                          // رنگ اصلی میانی پد دکمه
val BoosterFaceBottom: Color get() = BoosterPrimaryColor.darken(0.25f)        // شیب تاریک‌تر پایین پد دکمه
val BoosterFaceBorder: Color get() = BoosterPrimaryColor.lighten(0.50f)        // خط دور هایلایت روی پد دکمه
val BoosterButtonShadow: Color get() = BoosterPrimaryColor.darken(0.65f)      // حجم و سایه سه‌بعدی زیر دکمه

val BoosterRimGradientTop: Color get() = BoosterPrimaryColor.lighten(0.85f)    // هایلایت روشن بالای لبه بیرونی
val BoosterRimGradientMid: Color get() = BoosterPrimaryColor.lighten(0.40f)    // رنگ میانی لبه بیرونی
val BoosterRimGradientBottom: Color get() = BoosterPrimaryColor.darken(0.10f) // رنگ پایینی لبه بیرونی
val BoosterRimBorder: Color get() = BoosterPrimaryColor.lighten(0.45f)         // خط دور لبه بیرونی

// متغیرهای سازگاری
val BoosterRimBase: Color get() = BoosterRimGradientMid
val BoosterButtonHighlight: Color get() = BoosterFaceTop
val BoosterButtonBg: Color get() = BoosterFaceMid
val BoosterButtonDark: Color get() = BoosterFaceBottom
val BoosterBadgeGreenTop = Color(0xFF4ADE80)
val BoosterBadgeGreenBottom = Color(0xFF16A34A)

// --- دکمه بزرگ شروع بازی در منوی اصلی (Main Play Button) ---
// 🌟 این دکمه نیز فقط با تغییر همین یک رنگ اصلی کنترل می‌شود:
val PlayButtonColor = Color(0xFF65A30D)          // 🟢 رنگ اصلی دکمه شروع مرحله در منو
val PlayButtonFaceTop = PlayButtonColor.lighten(0.35f)
val PlayButtonFaceMid = PlayButtonColor
val PlayButtonFaceBottom = PlayButtonColor.darken(0.25f)
val PlayButtonFaceBorder = PlayButtonColor.lighten(0.50f)
val PlayButtonShadow = PlayButtonColor.darken(0.65f)


// --- ۷. رنگ‌های تاکیدی و درخشان (Accents) ---
val AccentGold = Color(0xFFFBBF24)       // رنگ طلایی سکه‌ها و جوایز
val PrimaryNeon = Color(0xFF8B5CF6)      // بنفش نئونی
val SecondaryNeon = Color(0xFF06B6D4)    // فیروزه‌ای نئونی
val SuccessGreen = Color(0xFF10B981)     // سبز موفقیت و تایید
val ErrorRed = Color(0xFFEF4444)         // قرمز خطا و باخت

// --- ۷. المان‌های عمومی و کارت‌های منو ---
val DarkCard: Color get() = maincolor2
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFFD1D5DB)
val BorderGlass = Color(0xFF3B2E6E)
val CardHighlight = Color(0xFF4C3E8A)

/**
 * رنگ‌بندی سه‌بعدی لبه دکمه بازی بر اساس درجه سختی مرحله
 */
data class DifficultyRimColors(
    val gradient: List<Color>,
    val border: Color,
    val baseShadow: Color
)

fun getDifficultyRimColors(difficulty: String): DifficultyRimColors {
    return when (difficulty) {
        "Kolay" -> DifficultyRimColors(
            gradient = listOf(Color(0xFFE8FDF0), Color(0xFF4ADE80), Color(0xFF16A34A)),
            border = Color(0xFF86EFAC),
            baseShadow = Color(0xFF14532D)
        )
        "Orta" -> DifficultyRimColors(
            gradient = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7)),
            border = Color(0xFF7DD3FC),
            baseShadow = Color(0xFF075985)
        )
        "Zor" -> DifficultyRimColors(
            gradient = listOf(Color(0xFFFFEDD5), Color(0xFFFB923C), Color(0xFFEA580C)),
            border = Color(0xFFFDBA74),
            baseShadow = Color(0xFF9A3412)
        )
        "CokZor" -> DifficultyRimColors(
            gradient = listOf(Color(0xFFFEE2E2), Color(0xFFF43F5E), Color(0xFFDC2626)),
            border = Color(0xFFFDA4AF),
            baseShadow = Color(0xFF881337)
        )
        else -> DifficultyRimColors(
            gradient = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF0284C7)),
            border = Color(0xFF7DD3FC),
            baseShadow = Color(0xFF075985)
        )
    }
}
