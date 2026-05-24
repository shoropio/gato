package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity to track game statistics for each game mode.
 * Modes: "pvp", "vs_ai_easy", "vs_ai_normal", "vs_ai_hard", "vs_ai_impossible", "demo"
 */
@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey val modeId: String,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val totalPlayed: Int = 0
)

/**
 * Entity to track customization themes unlocked by the player.
 * Themes: "theme_cyber_neon", "theme_vaporwave", "theme_golden_prestige", "theme_emerald_vault"
 * Avatars: "avatar_cyber_cat", "avatar_glitch_gamer", "avatar_minimax_bot", "avatar_pixel_ninja"
 */
@Entity(tableName = "unlocked_cosmetics")
data class UnlockedCosmeticEntity(
    @PrimaryKey val cosmeticId: String,
    val isUnlocked: Boolean = false
)

/**
 * Entity to track user achievements in the game.
 */
@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val achievementId: String,
    val title: String,
    val description: String,
    val isUnlocked: Boolean = false,
    val progress: Int = 0,
    val maxProgress: Int = 1,
    val timestamp: Long = 0L
)

/**
 * Entity to track game state/user preferences in Room.
 * Singleton table (id = 1).
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val soundOn: Boolean = true,
    val vibrationOn: Boolean = true,
    val darkThemeOn: Boolean = true,
    val dynamicColorsOn: Boolean = false,
    val boardStyle: String = "default", // default (Cyan/Magenta), vaporwave, gold, emerald
    val selectedAvatar: String = "avatar_cyber_cat"
)
