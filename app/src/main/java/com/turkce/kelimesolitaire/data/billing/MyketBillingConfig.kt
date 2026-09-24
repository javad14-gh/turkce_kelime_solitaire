package com.turkce.kelimesolitaire.data.billing

/**
 * Configuration and product definitions for Myket In-App Billing (پرداخت درون‌برنامه‌ای مایکت).
 */
object MyketBillingConfig {

    /**
     * Myket RSA Public Key for verifying purchase signatures.
     * کلید عمومی پرداخت مایکت را از پیشخان توسعه‌دهندگان مایکت (بخش پرداخت درون‌برنامه‌ای) کپی و اینجا قرار دهید.
     */
    const val MYKET_RSA_PUBLIC_KEY: String =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCOeG2wvb4ClXX1u/NRGuYZH2MQiPx2rfZ/4spQPdzkxLK27PskVTacz/ZmPOYwdttUQJHsyWaufIkbWZ84cEpfIVyoPo9657Xneb+WWeLptshHFN790UNmOOiewcc1x7GBP5Vm6Z9SQ7IsYipWfq29gNXyEd1+XNZFI0n1vFPs1wIDAQAB"

    // Myket Package Name for In-App Billing Service Intent
    const val MYKET_BILLING_PACKAGE = "ir.mservices.market"
    const val MYKET_BIND_ACTION = "ir.mservices.market.InAppBillingService.BIND"

    // Product SKUs (دقیقاً مشابه کافه بازار برای یکپارچگی کامل کدهای درون بازی)
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
