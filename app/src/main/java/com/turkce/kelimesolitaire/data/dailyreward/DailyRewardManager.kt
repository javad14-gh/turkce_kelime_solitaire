package com.turkce.kelimesolitaire.data.dailyreward

import android.content.Context
import android.content.SharedPreferences
import com.turkce.kelimesolitaire.analytics.Analytics
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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

    const val CYCLE_LENGTH = 7

    val TIERS: List<DailyRewardTier> = listOf(
        // Week 1
        DailyRewardTier(1, 50),
        DailyRewardTier(2, 75),
        DailyRewardTier(3, 100),
        DailyRewardTier(4, 125),
        DailyRewardTier(5, 150),
        DailyRewardTier(6, 175),
        DailyRewardTier(7, 350, RewardChestType.BRONZE)
    )

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private data class Resolved(val count: Int, val cycle: Int, val claimedToday: Boolean)

    /** Applies the streak rule. Does not write anything. */
    private fun resolve(prefs: SharedPreferences, today: LocalDate): Resolved {
        val lastDate = runCatching {
            LocalDate.parse(prefs.getString(KEY_LAST_CLAIM_DATE, "").orEmpty())
        }.getOrNull()
        // Old installs may hold a count up to 30: clamp to the new cycle length
        var count = prefs.getInt(KEY_CLAIMED_COUNT, 0).coerceAtMost(CYCLE_LENGTH)
        var cycle = prefs.getInt(KEY_CYCLE, 1)

        val claimedToday = lastDate != null && !lastDate.isBefore(today)

        if (!claimedToday) {
            if (count >= CYCLE_LENGTH) {
                count = 0          // finished a full week
                cycle += 1
            } else if (lastDate != null && ChronoUnit.DAYS.between(lastDate, today) > 1) {
                count = 0          // missed at least one day: streak broken
            }
        }
        return Resolved(count, cycle, claimedToday)
    }
    fun getDailyRewardState(context: Context): DailyRewardState {
        val prefs = getPrefs(context)
        val r = resolve(prefs, LocalDate.now())

        // Persist the normalization (streak reset / new cycle)
        prefs.edit().putInt(KEY_CLAIMED_COUNT, r.count).putInt(KEY_CYCLE, r.cycle).apply()

        val isReadyToClaim = !r.claimedToday
        val targetClaimDay =
            if (isReadyToClaim) (r.count + 1).coerceIn(1, CYCLE_LENGTH) else r.count.coerceIn(1, CYCLE_LENGTH)

        val items = TIERS.map { tier ->
            val status = when {
                tier.dayNumber <= r.count -> DailyRewardClaimStatus.CLAIMED
                isReadyToClaim && tier.dayNumber == targetClaimDay -> DailyRewardClaimStatus.READY_TO_CLAIM
                else -> DailyRewardClaimStatus.LOCKED
            }
            DailyRewardItemStatus(tier, status)
        }

        return DailyRewardState(
            currentClaimDay = targetClaimDay,
            claimedDaysCount = r.count,
            cycle = r.cycle,
            isReadyToClaimToday = isReadyToClaim,
            items = items
        )
    }

    fun claimDailyReward(context: Context, doubleReward: Boolean): Int {
        val prefs = getPrefs(context)
        val today = LocalDate.now()
        val r = resolve(prefs, today)
        if (r.claimedToday) return 0

        val rawCount = prefs.getInt(KEY_CLAIMED_COUNT, 0).coerceAtMost(CYCLE_LENGTH)
        val streakBroken = rawCount in 1 until CYCLE_LENGTH && r.count == 0

        val claimDay = (r.count + 1).coerceIn(1, CYCLE_LENGTH)
        val tier = TIERS.find { it.dayNumber == claimDay } ?: TIERS[0]
        val awarded = tier.coins * (if (doubleReward) 2 else 1)

        prefs.edit()
            .putString(KEY_LAST_CLAIM_DATE, today.toString())   // ISO yyyy-MM-dd, same as the old format
            .putInt(KEY_CLAIMED_COUNT, claimDay)
            .putInt(KEY_CYCLE, r.cycle)
            .apply()

        Analytics.log("daily_claim", mapOf(
            "day" to claimDay,
            "double" to doubleReward,
            "streak_broken" to streakBroken
        ))
        return awarded
    }
}
