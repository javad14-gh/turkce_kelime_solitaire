package com.turkce.kelimesolitaire.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// =====================================================================
// 🎨 پالت رنگ‌های جامع بازی (Game Color Palette)
// تمام رنگ‌های بازی در این فایل قرار دارند و شما می‌توانید کدهای هگزادسیمال
// آن‌ها را به دلخواه خود تغییر دهید.
// فرمت رنگ‌ها: Color(0xFFxxxxxx) که xxxxxx کد رنگ ۶ رقمی است.
// نکته: حتماً 0xFF در ابتدای کد رنگ باقی بماند تا رنگ کاملاً مات نمایش داده شود.
// =====================================================================

// --- ۱. پس‌زمینه‌های اصلی بازی ---
val DarkBg = Color(0xFF0D0921)            // پس‌زمینه منوی اصلی و لودینگ بازی (سرمه‌ای کهکشانی تیره)
val GameTableBg = Color(0xFFF3E5F5)       // رنگ پس‌زمینه میز بازی (کاملاً یکدست و ساده)
val GameTableBgTop = GameTableBg          // جهت سازگاری
val GameTableBgBottom = GameTableBg       // جهت سازگاری

// --- ۲. محل قرارگیری دسته‌بندی‌ها (باکس‌های بالای ستون‌ها - Category Slots) ---
val CategorySlotBg = Color(0x80E6BBFF)         // رنگ زمینه باکس خالی دسته‌بندی (یکدست و بدون گرادیان)
val CategorySlotBgTop = CategorySlotBg         // جهت سازگاری
val CategorySlotBgBottom = CategorySlotBg      // جهت سازگاری
val CategorySlotBorder = Color(0xFFE0AAFF)     // کادر دور باکس خالی دسته‌بندی
val CategorySlotContent = Color(0x507B2CBF)    // رنگ نوشته «کارت دسته» و آیکون تاج داخل باکس
val CategoryActiveBannerBg = Color(0xFFFF00FF) // نوار بالای کارت فعال دسته‌بندی (طلایی)
val CategorySlotActiveBg = Color(0xFFFFFFFF)   // رنگ بدنه کارت فعال در جایگاه بالا (محل نشستن کلمات مچ‌شده)
val CategorySlotInnerShadow = Color(0xFF9D4EDD) // رنگ سایه داخلی باکس خالی دسته‌بندی (ایجاد حس گود بودن و عمق سوراخ)
val CategorySlotMatchedBorder = CategoryActiveBannerBg // رنگ کادر جایگاه بالا وقتی کارت‌ها در آن قرار می‌گیرند (هماهنگ با نوار بالا)

// --- ۳. پشت کارت‌ها (Card Back) ---
val CardBackBg = Color(0xFF9D4EDD)            // رنگ ساده و یکدست پشت کارت‌ها
val CardBackGradientTop = CardBackBg          // جهت سازگاری
val CardBackGradientBottom = CardBackBg       // جهت سازگاری
val CardBackPattern = CardBackBg              // جهت سازگاری

// --- ۴. روی کارت‌ها (Card Front) ---
val CardFaceBg = Color(0xFFFFFFFF)            // رنگ زمینه کارت‌های کلمه و کارت‌های دسته‌بندی
val CardFaceText = Color(0xFF1E293B)          // رنگ متن کلمات روی کارت
val CardFaceInnerBorder = Color(0xFFC77DFF)   // رنگ کادر داخلی روی کارت‌های معمولی
val CardCategoryBorder = Color(0xFFFF00FF)    // رنگ کادر ضخیم دور کارت دسته‌بندی (شروع از لبه، بولد و مشخص)
val CardCategoryCrown = Color(0xFFffbe0b)     // رنگ تاج کارت دسته‌بندی
val CardCategoryText = Color(0xFFFF00FF)      // رنگ نوشته کارت دسته‌بندی (مثلاً پوشاک)
val CardCategoryBg = CardFaceBg               // جهت سازگاری (زمینه مانند سایر کارت‌ها)
val CardDraggingBorder = Color(0x80FF00FF)    // رنگ کادر دور کارت در حال جابجایی (Drag)
val CardSelectedBorder = Color(0x80FF00FF)    // رنگ کادر دور کارت در حالت انتخاب
val CardJokerBgTop = Color(0xFFFEF08A)        // رنگ زمینه کارت جوکر

// --- ۵. مخزن کارت‌ها در حالت بر زدن (Stock Recycle State) ---
val StockRecycleBg = Color(0x26C77DFF)        // رنگ پس‌زمینه مخزن خالی جهت بر زدن
val StockRecycleBorder = Color(0xFF9D4EDD)    // رنگ کادر مخزن در حالت بر زدن
val StockRecycleText = Color(0xFF5A189A)      // رنگ متن و آیکون «بر زدن»

// --- ۶. دکمه‌های کمکی و راهنما در پایین صفحه بازی (Booster Buttons) ---
val BoosterButtonBg = Color(0xFF3C096C)        // رنگ اصلی دکمه‌های راهنما
val BoosterButtonHighlight = Color(0xFF7E6EFA) // هایلایت روشن بالای دکمه
val BoosterButtonDark = Color(0xFF452CE8)      // رنگ شیب تاریک‌تر پایین دکمه
val BoosterButtonShadow = Color(0xFF231478)    // لبه سه‌بعدی و سایه ضخیم زیر دکمه
val BoosterRimBorder = Color(0xFFC4B5FD)       // نوار دور دکمه

// --- ۷. رنگ‌های تاکیدی و درخشان (Accents) ---
val AccentGold = Color(0xFFFBBF24)       // رنگ طلایی سکه‌ها و جوایز
val PrimaryNeon = Color(0xFF8B5CF6)      // بنفش نئونی
val SecondaryNeon = Color(0xFF06B6D4)    // فیروزه‌ای نئونی
val SuccessGreen = Color(0xFF10B981)     // سبز موفقیت و تایید
val ErrorRed = Color(0xFFEF4444)         // قرمز خطا و باخت

// --- ۷. المان‌های عمومی و کارت‌های منو ---
val DarkCard = Color(0xFF3C096C)
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
