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
        c1 = Color(0xFFEBEEFF), // یاسی بسیار ملایم
        c2 = Color(0xFF5271ff), // سرمه‌ای سلطنتی
        c3 = Color(0xFF859BFF), // بنفش شاهانه
        c4 = Color(0xFFFFCB70)  // طلایی گرم
    ),
    rose_twilight(
        id = "rose_twilight",
        titlePersian = "گلبهی غروب",
        titleTurkish = "Alacakaranlık Gül",
        c1 = Color(0xFFFCE8F1), // صورتی پودری بسیار ملایم (میز)
        c2 = Color(0xFF6B1B4C), // زرشکی-بنفش عمیق (المان‌ها و دکمه‌ها)
        c3 = Color(0xFFD6336C), // صورتی توت‌فرنگی پررنگ (پشت کارت‌ها)
        c4 = Color(0xFFFFCB70)  // طلایی گرم (تاکیدی)
    ),
    MINT_FRESH(
    id = "mint_fresh",
    titlePersian = "نعنایی تازه",
    titleTurkish = "Taze Nane",
    c1 = Color(0xFFE7FBF4), // نعنایی بسیار روشن (میز)
    c2 = Color(0xFF0F5C4C), // سبز صنوبری تیره (دکمه‌ها)
    c3 = Color(0xFFFF7A59), // مرجانی گرم، مکمل سبز (پشت کارت‌ها)
    c4 = Color(0xFFFFCB70)  // نارنجی-طلایی (تاکیدی)
    ),
    WARM_AMBER(
        id = "warm_amber",
        titlePersian = "چوبی گرم",
        titleTurkish = "Sıcak Ahşap",
        c1 = Color(0xFFFDF4E3), // کرم چوبی دنج
        c2 = Color(0xFF5D2E16), // قهوه‌ای چرم شیک
        c3 = Color(0xFFC05621), // تراکوتا / خرمایی
        c4 = Color(0xFFFFCB70), // طلایی کهربایی گرم
    )
}

/**
 * مدیر وضعیت تم بازی - تغییر آنی بدون نیاز به ری‌استارت
 */
object GameThemeManager {
    var currentTheme by mutableStateOf(ThemePreset.WARM_AMBER)
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
val mainBg = Color(0xFFFFF6E6)
val DarkMain = Color(0xFF734A2A)
val DarkBg = Color(0xFF5271ff)            // پس‌زمینه اصلی بازی (هماهنگ با هدر پاپ‌آپ‌ها)
val GameTableBg: Color get() = maincolor1       // رنگ پس‌زمینه میز بازی (کاملاً یکدست و ساده)


// --- پاپ‌آپ‌ها و دیالوگ‌ها ---

// سینی داخلی (روشن)
val PopupBg = mainBg                                   // 0xFFFFF6E6 پس‌زمینه‌ی سینی
val PopupTrayDepth = Color(0xFFD9BE9A)                 // لبه‌ی سه‌بعدی پایین سینی (کرم تیره)

// قاب بیرونی (تیره)
val PopupFrameBase = Color(0xFF3F2410)                 // لبه‌ی تیره‌ی پایین قاب (عمق)
val PopupFrameGradient = listOf(                       // گرادیانت قاب، از بالا به پایین
    Color(0xFFA9774C),                                 // هایلایت بالا
    DarkMain,                                          // 0xFF734A2A رنگ اصلی
    Color(0xFF5E3A20),                                 // میانه‌ی تیره‌تر
    Color(0xFF4A2C16)                                  // پایین
)
val PopupTitleOutline = Color(0xFF3F2410)              // خط دور عنوان روی قاب

// دکمه‌های منو (عریض) و مربعی (صدا/ویبره): یک پالت مشترک
val MenuBtnTop = Color(0xFFD9A066)                     // بالای گرادیانت
val MenuBtnMid = Color(0xFFC2803F)                     // میانه
val MenuBtnBottom = Color(0xFF9C5F2A)                  // پایین
val MenuBtnOutline = Color(0xFF5E3A20)                 // حاشیه و خط دور متن دکمه

// متن‌ها روی سینی روشن
val PopupTextPrimary = Color(0xFF3B2412)               // متن اصلی (تیره، خوانا)
val PopupTextSecondary = Color(0xFF6B4A33)             // متن ثانویه

// برای سازگاری با کد قبلی (کپسول «سکه موجود» و ...)
val PopupButtonBg = Color(0xFFF0DDBF)                  // کپسول روشن روی سینی
// --- هدر بالای صفحه بازی (سکه و عنوان مرحله) ---
val LevelTitleText = Color(0xFFFFFFFF)            // رنگ نوشته مرحله (مثلاً مرحله ۱)
val LevelTitleOutline: Color get() = maincolor2         // رنگ خط دور نوشته مرحله (قابل تنظیم به هر رنگ دلخواه)
val CoinBoxBorder: Color get() = maincolor4             // رنگ خط دور باکس سکه (هماهنگ با رنگ تاکیدی/طلایی)

// --- باکس تعداد حرکات باقی‌مانده (Moves Left Card) ---
val MovesCardBg: Color get() = maincolor2          // رنگ زمینه باکس حرکات (کاملاً مستقل از بقیه بخش‌های بازی)
val MovesCardBorder: Color get() = maincolor2      // رنگ کادر دور باکس حرکات
val MovesCountText = Color(0xFFFFFFFF)       // رنگ عدد تعداد حرکات

// --- ۲. محل قرارگیری دسته‌بندی‌ها (باکس‌های بالای ستون‌ها - Category Slots) ---
// 🌟 تمام رنگ‌های زیر به صورت خودکار از روی رنگ میز (GameTableBg) محاسبه می‌شوند:
val CategorySlotBg: Color get() = GameTableBg.darken(0.07f)          // رنگ زمینه باکس خالی دسته‌بندی (کمی گودتر از میز)
val CategorySlotBorder: Color get() = GameTableBg.darken(0.14f)      // کادر دور باکس خالی دسته‌بندی (مرز تراشیده شده)
val CategorySlotContent: Color get() = GameTableBg.darken(0.35f)     // رنگ نوشته «کارت دسته» و آیکون تاج داخل باکس
val CategorySlotInnerShadow: Color get() = GameTableBg.darken(0.40f) // رنگ سایه داخلی باکس خالی دسته‌بندی (حس عمق و سوراخ)
val CategoryActiveBannerBg: Color get() = maincolor4          // نوار بالای کارت فعال دسته‌بندی (طلایی)
val CategorySlotActiveBg = Color.White            // رنگ بدنه کارت فعال در جایگاه بالا (محل نشستن کلمات مچ‌شده)
val CategorySlotMatchedBorder: Color get() = CategoryActiveBannerBg  // رنگ کادر جایگاه بالا وقتی کارت‌ها در آن قرار می‌گیرند (هماهنگ با نوار بالا)

// --- ۳. پشت کارت‌ها (Card Back) ---
val CardBackBg: Color get() = maincolor3            // رنگ ساده و یکدست پشت کارت‌ها (متصل به maincolor3)
// --- ۴. روی کارت‌ها (Card Front) ---
val CardFaceBg = Color.White            // رنگ زمینه کارت‌های کلمه و کارت‌های دسته‌بندی
val CardFaceText: Color get() = maincolor2          // رنگ متن کلمات روی کارت
val CardFaceBorder: Color get() = maincolor1.darken(0.12f)  // رنگ کادر بیرونی بسیار نازک روی کارت‌های معمولی (هماهنگ خودکار با میز)
val CardFaceInnerBorder: Color get() = CardBackBg   // رنگ کادر داخلی روی کارت‌های معمولی
val CardCategoryBorder: Color get() = maincolor4    // رنگ کادر ضخیم دور کارت دسته‌بندی (شروع از لبه، بولد و مشخص)
val CardCategoryCrown: Color get() = maincolor4     // رنگ تاج کارت دسته‌بندی
val CardCategoryText: Color get() = CardFaceText           // رنگ نوشته کارت دسته‌بندی (مثلاً پوشاک)
val CardDraggingBorder: Color get() = maincolor4.copy(alpha = 0.6f)    // رنگ کادر دور کارت در حال جابجایی (Drag) با شفافیت ملایم
val CardSelectedBorder: Color get() = maincolor4.copy(alpha = 0.7f)    // رنگ کادر دور کارت در حالت انتخاب با شفافیت ملایم

// --- ۵. مخزن کارت‌ها در حالت بر زدن (Stock Recycle State) ---
val StockRecycleBg: Color get() = CategorySlotBg       // رنگ پس‌زمینه مخزن خالی جهت بر زدن
val StockRecycleBorder: Color get() = CategorySlotBorder   // رنگ کادر مخزن در حالت بر زدن
val StockRecycleText: Color get() = CategorySlotContent     // رنگ متن و آیکون «بر زدن»

// --- ۶. دکمه‌های کمکی و راهنما در پایین صفحه بازی (Booster Buttons) ---
// 🌟 کافیست فقط همین یک رنگ را تغییر دهید؛ تمام طیف‌های سه‌بعدی به صورت خودکار ساخته می‌شوند:
val BoosterPrimaryColor: Color get() = maincolor2      // 🟢 رنگ اصلی دکمه‌های راهنما (کافیست فقط این رنگ را تغییر دهید)


val BoosterRimGradientTop: Color get() = BoosterPrimaryColor.lighten(0.85f)    // هایلایت روشن بالای لبه بیرونی
val BoosterRimGradientMid: Color get() = BoosterPrimaryColor.lighten(0.40f)    // رنگ میانی لبه بیرونی


val BoosterBadgeGreenTop = Color(0xFF95FF0A)
val BoosterBadgeGreenBottom = Color(0xFF74CC00)


// --- ۷. رنگ‌های تاکیدی و درخشان (Accents) ---
val AccentGold = Color(0xFFFBBF24)       // رنگ طلایی سکه‌ها و جوایز
val PrimaryNeon = Color(0xFF8B5CF6)      // بنفش نئونی
val SecondaryNeon = Color(0xFF06B6D4)    // فیروزه‌ای نئونی
val ErrorRed = Color(0xFFEF4444)         // قرمز خطا و باخت

// --- ۷. المان‌های عمومی و کارت‌های منو ---
val DarkCard: Color get() = maincolor2
val TextPrimary = Color(0xFFF9FAFB)



