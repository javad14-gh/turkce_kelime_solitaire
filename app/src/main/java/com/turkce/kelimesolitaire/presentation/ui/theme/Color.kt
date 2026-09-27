package com.turkce.kelimesolitaire.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// =====================================================================
// 🎨 پالت رنگ‌های جامع بازی (Game Color Palette)
// تمام رنگ‌های بازی در این فایل قرار دارند و شما می‌توانید کدهای هگزادسیمال
// آن‌ها را به دلخواه خود تغییر دهید.
// فرمت رنگ‌ها: Color(0xFFxxxxxx) که xxxxxx کد رنگ ۶ رقمی است.
// نکته: حتماً 0xFF در ابتدای کد رنگ باقی بماند تا رنگ کاملاً مات نمایش داده شود.
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

// --- ۱. پس‌زمینه‌های اصلی بازی ---
val DarkBg = Color(0xFF131B0E)            // پس‌زمینه منوی اصلی و لودینگ بازی (سرمه‌ای کهکشانی تیره)
val GameTableBg = Color(0xFFF0EFFB)       // رنگ پس‌زمینه میز بازی (کاملاً یکدست و ساده)
val GameTableBgTop = GameTableBg          // جهت سازگاری
val GameTableBgBottom = GameTableBg       // جهت سازگاری

// --- هدر بالای صفحه بازی (سکه و عنوان مرحله) ---
val LevelTitleText = Color(0xFFFFFFFF)            // رنگ نوشته مرحله (مثلاً مرحله ۱)
val LevelTitleOutline = Color(0xFF221a6c)         // رنگ خط دور نوشته مرحله (قابل تنظیم به هر رنگ دلخواه)
val CoinBoxBorder = Color(0xFFFFD700)             // رنگ خط دور باکس سکه (باکس بدون پس‌زمینه)
val CoinTextColor = Color(0xFF221a6c)             // رنگ عدد سکه‌ها در بالای صفحه
val CoinTextOutline = Color.Transparent           // رنگ خط دور عدد سکه‌ها (اختیاری)

// --- ۲. محل قرارگیری دسته‌بندی‌ها (باکس‌های بالای ستون‌ها - Category Slots) ---
// 🌟 تمام رنگ‌های زیر به صورت خودکار از روی رنگ میز (GameTableBg) محاسبه می‌شوند:
val CategorySlotBg = GameTableBg.darken(0.07f)          // رنگ زمینه باکس خالی دسته‌بندی (کمی گودتر از میز)
val CategorySlotBgTop = CategorySlotBg                  // جهت سازگاری
val CategorySlotBgBottom = CategorySlotBg               // جهت سازگاری
val CategorySlotBorder = GameTableBg.darken(0.14f)      // کادر دور باکس خالی دسته‌بندی (مرز تراشیده شده)
val CategorySlotContent = GameTableBg.darken(0.35f)     // رنگ نوشته «کارت دسته» و آیکون تاج داخل باکس
val CategorySlotInnerShadow = GameTableBg.darken(0.40f) // رنگ سایه داخلی باکس خالی دسته‌بندی (حس عمق و سوراخ)
val CategoryActiveBannerBg = Color(0xFFFFCB70)          // نوار بالای کارت فعال دسته‌بندی (طلایی)
val CategorySlotActiveBg = Color(0xFFFFFFFF)            // رنگ بدنه کارت فعال در جایگاه بالا (محل نشستن کلمات مچ‌شده)
val CategorySlotMatchedBorder = CategoryActiveBannerBg  // رنگ کادر جایگاه بالا وقتی کارت‌ها در آن قرار می‌گیرند (هماهنگ با نوار بالا)

// --- ۳. پشت کارت‌ها (Card Back) ---
val CardBackBg = Color(0xFF6F1594)            // رنگ ساده و یکدست پشت کارت‌ها
val CardBackGradientTop = CardBackBg          // جهت سازگاری
val CardBackGradientBottom = CardBackBg       // جهت سازگاری
val CardBackPattern = CardBackBg              // جهت سازگاری

// --- ۴. روی کارت‌ها (Card Front) ---
val CardFaceBg = Color(0xFFFFFFFF)            // رنگ زمینه کارت‌های کلمه و کارت‌های دسته‌بندی
val CardFaceText = Color(0xFF221a6c)          // رنگ متن کلمات روی کارت
val CardFaceBorder = Color(0xFFE2E8F0)        // رنگ کادر بیرونی بسیار نازک روی کارت‌های معمولی
val CardFaceInnerBorder = CardBackBg   // رنگ کادر داخلی روی کارت‌های معمولی
val CardCategoryBorder = Color(0xFFFFCB70)    // رنگ کادر ضخیم دور کارت دسته‌بندی (شروع از لبه، بولد و مشخص)
val CardCategoryCrown = Color(0xFFffbe0b)     // رنگ تاج کارت دسته‌بندی
val CardCategoryText = CardFaceText           // رنگ نوشته کارت دسته‌بندی (مثلاً پوشاک)
val CardCategoryBg = CardFaceBg               // جهت سازگاری (زمینه مانند سایر کارت‌ها)
val CardDraggingBorder = Color(0x80FFCB70)    // رنگ کادر دور کارت در حال جابجایی (Drag)
val CardSelectedBorder = Color(0x80FFD700)    // رنگ کادر دور کارت در حالت انتخاب
val CardJokerBgTop = Color(0xFFFEF08A)        // رنگ زمینه کارت جوکر

// --- ۵. مخزن کارت‌ها در حالت بر زدن (Stock Recycle State) ---
val StockRecycleBg = CategorySlotBg       // رنگ پس‌زمینه مخزن خالی جهت بر زدن
val StockRecycleBorder = CategorySlotBorder   // رنگ کادر مخزن در حالت بر زدن
val StockRecycleText = CategorySlotContent     // رنگ متن و آیکون «بر زدن»

// --- ۶. دکمه‌های کمکی و راهنما در پایین صفحه بازی (Booster Buttons) ---
// 🌟 کافیست فقط همین یک رنگ را تغییر دهید؛ تمام طیف‌های سه‌بعدی به صورت خودکار ساخته می‌شوند:
val BoosterPrimaryColor = Color(0xFF221a6c)      // 🟢 رنگ اصلی دکمه‌های راهنما (کافیست فقط این رنگ را تغییر دهید)

// لایه‌های سه‌بعدی خودکار (Auto-Generated):
val BoosterFaceTop = BoosterPrimaryColor.lighten(0.35f)           // رنگ روشن براق بالای پد دکمه
val BoosterFaceMid = BoosterPrimaryColor                          // رنگ اصلی میانی پد دکمه
val BoosterFaceBottom = BoosterPrimaryColor.darken(0.25f)        // شیب تاریک‌تر پایین پد دکمه
val BoosterFaceBorder = BoosterPrimaryColor.lighten(0.50f)        // خط دور هایلایت روی پد دکمه
val BoosterButtonShadow = BoosterPrimaryColor.darken(0.65f)      // حجم و سایه سه‌بعدی زیر دکمه

val BoosterRimGradientTop = BoosterPrimaryColor.lighten(0.85f)    // هایلایت روشن بالای لبه بیرونی
val BoosterRimGradientMid = BoosterPrimaryColor.lighten(0.40f)    // رنگ میانی لبه بیرونی
val BoosterRimGradientBottom = BoosterPrimaryColor.darken(0.10f) // رنگ پایینی لبه بیرونی
val BoosterRimBorder = BoosterPrimaryColor.lighten(0.45f)         // خط دور لبه بیرونی

// متغیرهای سازگاری
val BoosterRimBase = BoosterRimGradientMid
val BoosterButtonHighlight = BoosterFaceTop
val BoosterButtonBg = BoosterFaceMid
val BoosterButtonDark = BoosterFaceBottom
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
val DarkCard = Color(0xFF221a6c)
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
