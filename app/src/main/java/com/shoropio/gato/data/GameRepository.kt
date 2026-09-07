package com.shoropio.gato.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.lang.Math.max
import kotlin.math.max

enum class GameResult {
    WIN, LOSS, DRAW
}

class GameRepository(private val gameDao: GameDao) {

    val allStats: Flow<List<GameStatsEntity>> = gameDao.getAllStatsFlow()
    val allCosmetics: Flow<List<UnlockedCosmeticEntity>> = gameDao.getAllCosmeticsFlow()
    val allAchievements: Flow<List<AchievementEntity>> = gameDao.getAllAchievementsFlow()
    val settings: Flow<UserSettingsEntity?> = gameDao.getSettingsFlow()

    suspend fun getSettings(): UserSettingsEntity? {
        return gameDao.getSettings()
    }

    suspend fun saveSettings(userSettings: UserSettingsEntity) {
        gameDao.insertSettings(userSettings)
        if (FirebaseManager.isSignedIn()) {
            FirebaseManager.syncSettings(userSettings)
        }
    }

    suspend fun unlockCosmetic(cosmeticId: String) {
        gameDao.unlockCosmetic(cosmeticId)
        if (FirebaseManager.isSignedIn()) {
            FirebaseManager.syncCosmetic(UnlockedCosmeticEntity(cosmeticId, isUnlocked = true))
        }
    }

    suspend fun isCosmeticUnlocked(cosmeticId: String): Boolean {
        if (cosmeticId == "theme_cyber_neon" || cosmeticId == "avatar_cyber_cat") return true
        return gameDao.getCosmetic(cosmeticId)?.isUnlocked == true
    }

    suspend fun insertOrUpdateStats(stats: GameStatsEntity) {
        gameDao.insertOrUpdateStats(stats)
        if (FirebaseManager.isSignedIn()) {
            FirebaseManager.syncStats(stats)
        }
    }

    suspend fun getAchievement(id: String): AchievementEntity? {
        return gameDao.getAchievement(id)
    }

    suspend fun insertAchievement(achievement: AchievementEntity) {
        gameDao.insertAchievement(achievement)
        if (FirebaseManager.isSignedIn()) {
            FirebaseManager.syncAchievement(achievement)
        }
    }

    suspend fun updateAchievementProgress(id: String, progress: Int) {
        gameDao.updateAchievementProgress(id, progress)
        if (FirebaseManager.isSignedIn()) {
            val existing = gameDao.getAchievement(id) ?: return
            FirebaseManager.syncAchievement(existing)
        }
    }

    suspend fun unlockAchievement(id: String) {
        gameDao.unlockAchievement(id, System.currentTimeMillis())
        if (FirebaseManager.isSignedIn()) {
            val existing = gameDao.getAchievement(id) ?: return
            FirebaseManager.syncAchievement(existing)
        }
    }

    /**
     * Updates statistics of a specific mode when a game concludes.
     * Manages wins, losses, draws, current streak, and max streak.
     */
    suspend fun recordGameResult(modeId: String, result: GameResult) {
        val currentStats = gameDao.getStatsByMode(modeId) ?: GameStatsEntity(modeId = modeId)
        
        val newWins = if (result == GameResult.WIN) currentStats.wins + 1 else currentStats.wins
        val newLosses = if (result == GameResult.LOSS) currentStats.losses + 1 else currentStats.losses
        val newDraws = if (result == GameResult.DRAW) currentStats.draws + 1 else currentStats.draws
        
        val newCurrentStreak = when (result) {
            GameResult.WIN -> currentStats.currentStreak + 1
            GameResult.LOSS -> 0
            GameResult.DRAW -> currentStats.currentStreak // draws preserve the streak but don't increment
        }
        
        val newMaxStreak = kotlin.math.max(currentStats.maxStreak, newCurrentStreak)
        val newTotal = currentStats.totalPlayed + 1

        val updatedStats = GameStatsEntity(
            modeId = modeId,
            wins = newWins,
            losses = newLosses,
            draws = newDraws,
            currentStreak = newCurrentStreak,
            maxStreak = newMaxStreak,
            totalPlayed = newTotal
        )

        insertOrUpdateStats(updatedStats)

        // Trigger achievement evaluations based on general gameplay results
        evaluateSystemAchievements(modeId, updatedStats)
    }

    /**
     * Internal helper to examine if a freshly saved game statistic triggers achievements.
     */
    private suspend fun evaluateSystemAchievements(modeId: String, stats: GameStatsEntity) {
        // Achievement: Play first game (Play first game overall)
        unlockAchievement("ach_first_steps")

        // Achievement: streak of 3
        if (stats.currentStreak >= 3) {
            unlockAchievement("ach_streak_3")
        }

        // Mode specific achievements
        if (modeId == "pvp") {
            // Achievement: PvP champion (5 PvP wins)
            updateAchievementProgress("ach_pvp_champion", stats.wins)
            if (stats.wins >= 5) {
                unlockAchievement("ach_pvp_champion")
            }
        }

        if (modeId == "vs_ai_normal" || modeId == "vs_ai_hard" || modeId == "vs_ai_impossible") {
            if (stats.wins >= 1) {
                unlockAchievement("ach_beat_normal")
            }
        }

        if (modeId == "vs_ai_impossible") {
            if (stats.wins >= 1 || stats.draws >= 1) {
                unlockAchievement("ach_beast_mode")
                // Unlock reward theme: Golden Prestige
                unlockCosmetic("theme_golden_prestige")
            }
        }

        if (modeId == "demo") {
            unlockAchievement("ach_demo_watcher")
        }
    }
}
