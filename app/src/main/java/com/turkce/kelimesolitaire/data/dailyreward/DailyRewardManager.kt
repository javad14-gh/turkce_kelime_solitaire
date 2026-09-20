package com.turkce.kelimesolitaire.data.dailyreward

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RewardChestType {
    NONE,
    BRONZE,
    SILVER,
    GOLD,
    RUBY,
    DIAMOND
}

enum class DailyRewardClaimStatus {
    CLAIMED,
    READY_TO_CLAIM,
    LOCKED
}

data class DailyRewardTier(
    val dayNumber: Int,
    val coins: Int,
    val chestType: RewardChestType = RewardChestType.NONE
)

data class DailyRewardItemStatus(
    val tier: DailyRewardTier,
    val status: DailyRewardClaimStatus
)

data class DailyRewardState(
    val currentClaimDay: Int, // 1..30 (the day to be claimed today, or just claimed)
    val claimedDaysCount: Int, // 0..30
    val cycle: Int, // 1, 2, ...
    val isReadyToClaimToday: Boolean,
    val items: List<DailyRewardItemStatus>
)

object DailyRewardManager {
    private const val PREFS_NAME = "turkce_solitaire_daily_rewards"
    private const val KEY_LAST_CLAIM_DATE = "daily_reward_last_claim_date"
    private const val KEY_CLAIMED_COUNT = "daily_reward_claimed_count"
    private const val KEY_CYCLE = "daily_reward_cycle"

    val TIERS: List<DailyRewardTier> = listOf(
        // Week 1
        DailyRewardTier(1, 50),
        DailyRewardTier(2, 75),
        DailyRewardTier(3, 100),
        DailyRewardTier(4, 125),
        DailyRewardTier(5, 150),
        DailyRewardTier(6, 175),
        DailyRewardTier(7, 350, RewardChestType.BRONZE),

        // Week 2
        DailyRewardTier(8, 200),
        DailyRewardTier(9, 225),
        DailyRewardTier(10, 250),
        DailyRewardTier(11, 275),
        DailyRewardTier(12, 300),
        DailyRewardTier(13, 325),
        DailyRewardTier(14, 600, RewardChestType.SILVER),

        // Week 3
        DailyRewardTier(15, 350),
        DailyRewardTier(16, 375),
        DailyRewardTier(17, 400),
        DailyRewardTier(18, 425),
        DailyRewardTier(19, 450),
        DailyRewardTier(20, 475),
        DailyRewardTier(21, 900, RewardChestType.GOLD),

        // Week 4 & Grand Finale
        DailyRewardTier(22, 500),
        DailyRewardTier(23, 500),
        DailyRewardTier(24, 550),
        DailyRewardTier(25, 550),
        DailyRewardTier(26, 600),
        DailyRewardTier(27, 600),
        DailyRewardTier(28, 1000, RewardChestType.RUBY),
        DailyRewardTier(29, 750),
        DailyRewardTier(30, 2000, RewardChestType.DIAMOND)
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    fun getDailyRewardState(context: Context): DailyRewardState {
        val prefs = getPrefs(context)
        val lastClaimDate = prefs.getString(KEY_LAST_CLAIM_DATE, "") ?: ""
        var claimedCount = prefs.getInt(KEY_CLAIMED_COUNT, 0)
        var cycle = prefs.getInt(KEY_CYCLE, 1)

        val todayDate = getTodayDateString()
        val isClaimedToday = (lastClaimDate == todayDate)

        // If today is a new day and previous cycle completed 30 days, reset count for new cycle
        if (!isClaimedToday && claimedCount >= 30) {
            claimedCount = 0
            cycle += 1
            prefs.edit()
                .putInt(KEY_CLAIMED_COUNT, 0)
                .putInt(KEY_CYCLE, cycle)
                .apply()
        }

        val isReadyToClaim = !isClaimedToday
        val targetClaimDay = if (isReadyToClaim) (claimedCount + 1).coerceIn(1, 30) else claimedCount.coerceIn(1, 30)

        val items = TIERS.map { tier ->
            val status = when {
                tier.dayNumber <= claimedCount -> DailyRewardClaimStatus.CLAIMED
                isReadyToClaim && tier.dayNumber == targetClaimDay -> DailyRewardClaimStatus.READY_TO_CLAIM
                else -> DailyRewardClaimStatus.LOCKED
            }
            DailyRewardItemStatus(tier, status)
        }

        return DailyRewardState(
            currentClaimDay = targetClaimDay,
            claimedDaysCount = claimedCount,
            cycle = cycle,
            isReadyToClaimToday = isReadyToClaim,
            items = items
        )
    }

    fun claimDailyReward(context: Context, doubleReward: Boolean): Int {
        val prefs = getPrefs(context)
        val todayDate = getTodayDateString()
        val lastClaimDate = prefs.getString(KEY_LAST_CLAIM_DATE, "") ?: ""

        if (lastClaimDate == todayDate) {
            return 0 // Already claimed today
        }

        var claimedCount = prefs.getInt(KEY_CLAIMED_COUNT, 0)
        var cycle = prefs.getInt(KEY_CYCLE, 1)

        if (claimedCount >= 30) {
            claimedCount = 0
            cycle += 1
        }

        val claimDayIndex = (claimedCount + 1).coerceIn(1, 30)
        val tier = TIERS.find { it.dayNumber == claimDayIndex } ?: TIERS[0]
        val awardedCoins = tier.coins * (if (doubleReward) 2 else 1)

        val newClaimedCount = claimDayIndex
        prefs.edit()
            .putString(KEY_LAST_CLAIM_DATE, todayDate)
            .putInt(KEY_CLAIMED_COUNT, newClaimedCount)
            .putInt(KEY_CYCLE, cycle)
            .apply()

        return awardedCoins
    }
}
