package com.turkce.kelimesolitaire.data.billing

/**
 * Configuration and product definitions for Cafe Bazaar In-App Billing (پرداخت درون‌برنامه‌ای کافه بازار).
 */
object BazaarBillingConfig {

    /**
     * Cafe Bazaar RSA Public Key for verifying purchase signatures.
     */
    const val BAZAAR_RSA_PUBLIC_KEY: String =
        "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDC0Q7dm9gEUwmnbF2EwC2vJBtlWFMCsZxYL+Q7bzolufIIh6UlLsDot7Dlopd7AizBV2Ku5MygqHJ2SwvE8cTY8nSic8V23pY0kpcqq2PJpcQBjRkvdC8NQB1vJ8//dWpxwrUjmje93tUbrp8SaDWtKwI8Pda/pgzCr4sjTLjgbO9QCCRWOl+37KDYD7ZIYQb9eaeZquMLi1w+hiIszWYwL1bfhaY91PrN3x79tocCAwEAAQ=="

    // Product SKUs (شناسه محصولات برای تعریف در پیشخان بازار)
    const val SKU_REMOVE_ADS = "remove_ads"
    const val SKU_STARTER_PACK = "starter_pack"
    const val SKU_BUNDLE_MEGA = "bundle_mega"
    const val SKU_BUNDLE_SPECIAL = "bundle_special"
    const val SKU_BUNDLE_ECONOMY = "bundle_economy"
    const val SKU_COINS_500 = "coins_500"
    const val SKU_COINS_1200 = "coins_1200"
    const val SKU_COINS_3000 = "coins_3000"

    data class ProductInfo(
        val sku: String,
        val titleFa: String,
        val priceTomans: Int,
        val priceRials: Long = priceTomans * 10L,
        val isConsumable: Boolean,
        val description: String
    )

    val ALL_PRODUCTS: List<ProductInfo> = listOf(
        ProductInfo(
            sku = SKU_REMOVE_ADS,
            titleFa = "حذف تبلیغات",
            priceTomans = 39_000,
            isConsumable = false,
            description = "حذف دائمی تمامی تبلیغات میان‌صفحه‌ای و بنری بازی"
        ),
        ProductInfo(
            sku = SKU_STARTER_PACK,
            titleFa = "بسته شروع",
            priceTomans = 99_000,
            isConsumable = false,
            description = "پیشنهاد ویژه یکبار خرید: ۲۰۰۰ سکه + ۳ عدد از هر کمکی + حذف دائمی تبلیغات"
        ),
        ProductInfo(
            sku = SKU_BUNDLE_MEGA,
            titleFa = "بسته مگا",
            priceTomans = 149_000,
            isConsumable = true,
            description = "۴۰۰۰ سکه + ۴ عدد راهنما، ۴ عدد بازگشت، ۴ عدد جوکر"
        ),
        ProductInfo(
            sku = SKU_BUNDLE_SPECIAL,
            titleFa = "بسته ویژه",
            priceTomans = 89_000,
            isConsumable = true,
            description = "۱۰۰۰ سکه + ۲ عدد راهنما، ۲ عدد بازگشت، ۲ عدد جوکر"
        ),
        ProductInfo(
            sku = SKU_BUNDLE_ECONOMY,
            titleFa = "بسته اقتصادی",
            priceTomans = 49_000,
            isConsumable = true,
            description = "۵۰۰ سکه + ۱ عدد راهنما، ۱ عدد بازگشت، ۱ عدد جوکر"
        ),
        ProductInfo(
            sku = SKU_COINS_500,
            titleFa = "۵۰۰ سکه",
            priceTomans = 19_000,
            isConsumable = true,
            description = "بسته کوچک ۵۰۰ سکه طلای بازی"
        ),
        ProductInfo(
            sku = SKU_COINS_1200,
            titleFa = "۱۲۰۰ سکه",
            priceTomans = 39_000,
            isConsumable = true,
            description = "بسته محبوب ۱۲۰۰ سکه طلای بازی"
        ),
        ProductInfo(
            sku = SKU_COINS_3000,
            titleFa = "۳۰۰۰ سکه",
            priceTomans = 79_000,
            isConsumable = true,
            description = "صندوقچه بزرگ ۳۰۰۰ سکه طلای بازی (بهترین ارزش)"
        )
    )
}
