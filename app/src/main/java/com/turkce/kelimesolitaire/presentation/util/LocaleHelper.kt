package com.turkce.kelimesolitaire.presentation.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

object LocaleHelper {

    fun isPersian(context: Context): Boolean {
        return context.packageName.contains("persian", ignoreCase = true) ||
                (try {
                    val buildConfigClass = Class.forName("com.turkce.kelimesolitaire.BuildConfig")
                    val flavorField = buildConfigClass.getField("FLAVOR")
                    flavorField.get(null) == "bazaar"
                } catch (e: Exception) {
                    false
                })
    }

    fun toPersianDigits(text: String): String =
        text.map { char ->
            when (char) {
                '0' -> '۰'
                '1' -> '۱'
                '2' -> '۲'
                '3' -> '۳'
                '4' -> '۴'
                '5' -> '۵'
                '6' -> '۶'
                '7' -> '۷'
                '8' -> '۸'
                '9' -> '۹'
                else -> char
            }
        }.joinToString("")

    fun formatNumber(number: Int, isPersian: Boolean): String =
        if (isPersian) toPersianDigits(number.toString()) else number.toString()

    fun formatNumber(text: String, isPersian: Boolean): String =
        if (isPersian) toPersianDigits(text) else text

    // Localized Strings
    fun playButton(isPersian: Boolean): String =
        if (isPersian) "شروع بازی" else "OYNA"

    fun levelTitle(level: Int, isPersian: Boolean): String =
        if (isPersian) "مرحله ${formatNumber(level, true)}" else "SEVİYE $level"

    fun difficulty(diff: String, isPersian: Boolean): String =
        if (!isPersian) diff else when (diff) {
            "Kolay" -> "آسان"
            "Orta" -> "متوسط"
            "Zor" -> "سخت"
            "CokZor" -> "خیلی سخت"
            else -> diff
        }

    fun remainingMovesTitle(isPersian: Boolean): String =
        if (isPersian) "حرکات باقی‌مانده" else "KALAN HAMLE"

    fun emptySlot(isPersian: Boolean): String =
        if (isPersian) "خالی" else "Boş"

    fun settingsTitle(isPersian: Boolean): String =
        if (isPersian) "تنظیمات" else "Ayarlar"

    fun privacyPolicy(isPersian: Boolean): String =
        if (isPersian) "حریم خصوصی" else "Gizlilik Politikası"

    fun storeTitle(isPersian: Boolean): String =
        if (isPersian) "فروشگاه" else "Mağaza"

    fun storeHeader(isPersian: Boolean): String =
        if (isPersian) "فروشگاه و جوایز" else "MAĞAZA & ÖDÜLLER"

    fun removeAdsTitle(isPersian: Boolean): String =
        if (isPersian) "حذف تبلیغات" else "REKLAMLARI KALDIR"

    fun removeAdsDesc(isPersian: Boolean): String =
        if (isPersian) "حذف دائمی تمام تبلیغات درون بازی!\u200F" else "Tüm zorunlu ve geçiş reklamlarını sonsuza dek kaldır!"

    fun removeAdsPrice(isPersian: Boolean): String =
        if (isPersian) "۳۹,۰۰۰ تومان" else "₺39.99"

    fun activeStatus(isPersian: Boolean): String =
        if (isPersian) "فعال" else "AKTİF"

    fun coinPacksTitle(isPersian: Boolean): String =
        if (isPersian) "بسته‌های سکه" else "ALTIN PAKETLERİ"

    fun freeCoinsTitle(isPersian: Boolean): String =
        if (isPersian) "سکه رایگان" else "ÜCRETSİZ ALTIN"

    fun freeCoinsDesc(isPersian: Boolean): String =
        if (isPersian) "با تماشای ویدیوی کوتاه ۵۰ سکه هدیه بگیرید!\u200F" else "Kısa bir video izle ve hemen 50 Altın kazan!"

    fun watchAndEarn(isPersian: Boolean): String =
        if (isPersian) "تماشا و دریافت" else "İZLE VE KAZAN"

    fun freeBadge(isPersian: Boolean): String =
        if (isPersian) "رایگان" else "Ücretsiz"

    fun freeCoinsRewardText(isPersian: Boolean): String =
        if (isPersian) "+۵۰ سکه" else "+50 Altın"

    // Coin Packs
    fun pack1Title(isPersian: Boolean): String = if (isPersian) "کیسه سکه" else "Kese Dolusu"
    fun pack1Price(isPersian: Boolean): String = if (isPersian) "۱۹,۰۰۰ تومان" else "₺19.99"

    fun pack2Title(isPersian: Boolean): String = if (isPersian) "صندوقچه سکه" else "Sandık Dolusu"
    fun pack2Price(isPersian: Boolean): String = if (isPersian) "۴۹,۰۰۰ تومان" else "₺49.99"

    fun pack3Title(isPersian: Boolean): String = if (isPersian) "گنجینه سکه" else "Hazine Dolusu"
    fun pack3Price(isPersian: Boolean): String = if (isPersian) "۹۹,۰۰۰ تومان" else "₺99.99"

    // Gameplay Dialogs
    fun outOfMovesTitle(isPersian: Boolean): String =
        if (isPersian) "فرصت‌ها تمام شد!\u200F" else "Hamleler Bitti!"

    fun outOfMovesPrompt(isPersian: Boolean): String =
        if (isPersian) "آیا می‌خواهید با ۵۰ سکه، ۵ حرکت دیگر دریافت کنید؟\u200F" else "50 Altın harcayarak 5 ek hamle almak ister misiniz?"

    fun giveUp(isPersian: Boolean): String =
        if (isPersian) "انصراف" else "Pes Et"

    fun getMoreMoves(isPersian: Boolean): String =
        if (isPersian) "+۵ حرکت بگیر" else "+5 Hamle Al"

    fun categoryCompleted(isPersian: Boolean): String =
        if (isPersian) "دسته تکمیل شد!\u200F" else "Kategori Tamamlandı!"

    fun nextLevel(isPersian: Boolean): String =
        if (isPersian) "مرحله بعدی" else "Sonraki Seviye"

    fun retry(isPersian: Boolean): String =
        if (isPersian) "تلاش مجدد" else "Tekrar Oyna"

    fun mainMenu(isPersian: Boolean): String =
        if (isPersian) "منوی اصلی" else "Ana Menü"

    fun victoryTitle(isPersian: Boolean): String =
        if (isPersian) "عالی بود!\u200F" else "Tebrikler!"

    fun victorySubtitle(level: Int, isPersian: Boolean): String =
        if (isPersian) "مرحله ${formatNumber(level, true)} تکمیل شد\u200F" else "Seviye $level Tamamlandı"

    fun defeatTitle(isPersian: Boolean): String =
        if (isPersian) "فرصت‌ها تمام شد!\u200F" else "Hamleler Bitti!"

    fun defeatSubtitle(level: Int, isPersian: Boolean): String =
        if (isPersian) "فرصت‌های شما در مرحله ${formatNumber(level, true)} به پایان رسید.\u200F" else "Seviye $level içinde hamleniz bitti."

    fun continueForCoins(isPersian: Boolean): String =
        if (isPersian) "ادامه بازی (+۱۵ فرصت)" else "Oyuna Devam Et (+15 Hamle)"

    fun dailyRewardTitle(isPersian: Boolean): String =
        if (isPersian) "جایزه ورود روزانه" else "Günlük Giriş Ödülü"

    fun dailyRewardSubtitle(cycle: Int, day: Int, isPersian: Boolean): String =
        if (isPersian) "دوره ${formatNumber(cycle, true)} • روز ${formatNumber(day, true)} از ۳۰" else "Döngü $cycle • $day / 30. Gün"

    fun claim(isPersian: Boolean): String =
        if (isPersian) "دریافت هدیه" else "Ödülü Al"

    fun claim2x(isPersian: Boolean): String =
        if (isPersian) "۲ برابر با ویدیو 🎬" else "2 Katı Al 🎬"

    fun comeBackTomorrow(isPersian: Boolean): String =
        if (isPersian) "فردا بازگردید!" else "Yarın Tekrar Gel!"

    fun dayLabel(day: Int, isPersian: Boolean): String =
        if (isPersian) "روز ${formatNumber(day, true)}" else "${day}. Gün"

    fun milestoneChestLabel(isPersian: Boolean): String =
        if (isPersian) "صندوقچه هدیه" else "Özel Sandık"

    fun claimedText(isPersian: Boolean): String =
        if (isPersian) "دریافت شد" else "Alındı"
}

@Composable
fun rememberAppFont(): FontFamily {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    return remember(isPersian, context) {
        try {
            if (isPersian) {
                FontFamily(Font(path = "fonts/vazirmatn.ttf", assetManager = context.assets))
            } else {
                FontFamily(Font(path = "fonts/nunito_black.ttf", assetManager = context.assets))
            }
        } catch (e: Throwable) {
            FontFamily.SansSerif
        }
    }
}
