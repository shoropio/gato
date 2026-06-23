package com.shoropio.gato.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface GameDao {
    // 1. Stats Queries
    @Query("SELECT * FROM game_stats WHERE modeId = :modeId")
    suspend fun getStatsByMode(modeId: String): GameStatsEntity?

    @Query("SELECT * FROM game_stats")
    fun getAllStatsFlow(): Flow<List<GameStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: GameStatsEntity)

    // 2. Customization/Cosmetics Queries
    @Query("SELECT * FROM unlocked_cosmetics")
    fun getAllCosmeticsFlow(): Flow<List<UnlockedCosmeticEntity>>

    @Query("SELECT * FROM unlocked_cosmetics WHERE cosmeticId = :cosmeticId")
    suspend fun getCosmetic(cosmeticId: String): UnlockedCosmeticEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCosmetic(cosmetic: UnlockedCosmeticEntity)

    @Query("UPDATE unlocked_cosmetics SET isUnlocked = 1 WHERE cosmeticId = :cosmeticId")
    suspend fun unlockCosmetic(cosmeticId: String)

    // 3. Achievements Queries
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT isUnlocked FROM achievements WHERE achievementId = :id")
    suspend fun isAchievementUnlocked(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Query("UPDATE achievements SET isUnlocked = 1, progress = maxProgress, timestamp = :timestamp WHERE achievementId = :id")
    suspend fun unlockAchievement(id: String, timestamp: Long)

    @Query("UPDATE achievements SET progress = :progress WHERE achievementId = :id")
    suspend fun updateAchievementProgress(id: String, progress: Int)

    // 4. Settings Queries
    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getSettings(): UserSettingsEntity?

    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)
}

@Database(
    entities = [
        GameStatsEntity::class,
        UnlockedCosmeticEntity::class,
        AchievementEntity::class,
        UserSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "gato_futurista_db"
                )
                .addCallback(GameDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class GameDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.gameDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: GameDao) {
            // Populate game stats for all 6 modes
            val modes = listOf("pvp", "vs_ai_easy", "vs_ai_normal", "vs_ai_hard", "vs_ai_impossible", "demo")
            modes.forEach { mode ->
                dao.insertOrUpdateStats(GameStatsEntity(modeId = mode))
            }

            // Populate system customizations (default unlocked / lockable themes)
            val cosmetics = listOf(
                UnlockedCosmeticEntity("theme_cyber_neon", isUnlocked = true), // default theme
                UnlockedCosmeticEntity("theme_vaporwave", isUnlocked = true),
                UnlockedCosmeticEntity("theme_golden_prestige", isUnlocked = false),
                UnlockedCosmeticEntity("theme_emerald_vault", isUnlocked = true),
                
                UnlockedCosmeticEntity("avatar_cyber_cat", isUnlocked = true), // default unlocked
                UnlockedCosmeticEntity("avatar_glitch_gamer", isUnlocked = false),
                UnlockedCosmeticEntity("avatar_minimax_bot", isUnlocked = false),
                UnlockedCosmeticEntity("avatar_pixel_ninja", isUnlocked = false)
            )
            cosmetics.forEach { dao.insertCosmetic(it) }

            // Populate Achievements
            val achievements = listOf(
                AchievementEntity(
                    "ach_first_steps",
                    "Primeros Pasos",
                    "Juega tu primera partida de Gato en cualquier modo.",
                    maxProgress = 1
                ),
                AchievementEntity(
                    "ach_pvp_champion",
                    "Campeón de Sofá",
                    "Gana 5 partidas en modo Jugador vs Jugador.",
                    maxProgress = 5
                ),
                AchievementEntity(
                    "ach_beat_normal",
                    "Cerebro Orgánico",
                    "Derrota a la IA en dificultad Normal o superior.",
                    maxProgress = 1
                ),
                AchievementEntity(
                    "ach_beast_mode",
                    "Hacker de Código",
                    "Empata o derrota al algoritmo Imposible en modo vs IA.",
                    maxProgress = 1
                ),
                AchievementEntity(
                    "ach_streak_3",
                    "Hiper-Racha",
                    "Consigue una racha de 3 victorias consecutivas.",
                    maxProgress = 3
                ),
                AchievementEntity(
                    "ach_demo_watcher",
                    "Espectador del Futuro",
                    "Mira una demostración completa de IA vs IA.",
                    maxProgress = 1
                )
            )
            achievements.forEach { dao.insertAchievement(it) }

            // Populate initial Settings
            dao.insertSettings(UserSettingsEntity())
        }
    }
}
