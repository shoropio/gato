package com.shoropio.gato.data

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.shoropio.gato.notification.GatoMessagingService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

@Suppress("UNCHECKED_CAST")

data class OnlineUser(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val isOnline: Boolean = false,
    val photoUrl: String? = null
)

data class GameMatch(
    val id: String = "",
    val playerX: String = "",
    val playerO: String = "",
    val playerXName: String = "",
    val playerOName: String = "",
    val board: List<String> = List(9) { "" },
    val currentPlayer: String = "X",
    val status: String = "waiting",
    val winner: String? = null
)

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private const val WEB_CLIENT_ID = "332656003926-lbu2v23sgmb2bmv9jhqju6kenrbrndl1.apps.googleusercontent.com"

    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore

    fun isSignedIn(): Boolean = auth.currentUser != null

    fun getCurrentUid(): String? = auth.currentUser?.uid

    fun getCurrentDisplayName(): String? = auth.currentUser?.displayName

    fun getCurrentPhotoUrl(): String? = auth.currentUser?.photoUrl?.toString()

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun signInWithGoogle(idToken: String): Result<String> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        result.user?.uid ?: throw Exception("No se pudo autenticar con Google")
    }

    suspend fun createUserProfile() {
        val uid = getCurrentUid() ?: return
        val user = auth.currentUser ?: return
        val token = GatoMessagingService.fcmToken
        val displayName = user.displayName ?: "Jugador"
        val profile = mutableMapOf<String, Any>(
            "displayName" to displayName,
            "searchName" to displayName.lowercase(),
            "isOnline" to true,
            "lastSeen" to FieldValue.serverTimestamp()
        )
        user.email?.let { profile["email"] = it.lowercase() }
        user.photoUrl?.let { profile["photoUrl"] = it.toString() }
        if (token != null) {
            profile["fcmToken"] = token
        }
        db.collection("users").document(uid).set(profile).await()
    }

    fun signOut(context: Context) {
        auth.signOut()
        getGoogleSignInClient(context).signOut()
    }

    suspend fun setOnlineStatus(isOnline: Boolean) {
        val uid = getCurrentUid() ?: return
        db.collection("users").document(uid).update(
            "isOnline", isOnline,
            "lastSeen", FieldValue.serverTimestamp()
        ).await()
    }

    suspend fun searchUsers(query: String): List<OnlineUser> {
        if (query.isBlank()) return emptyList()
        val currentUid = getCurrentUid()
        val q = query.lowercase()

        // Search by displayName prefix (case-insensitive via searchName field)
        val nameSnapshot = db.collection("users")
            .whereGreaterThanOrEqualTo("searchName", q)
            .whereLessThanOrEqualTo("searchName", q + "\uf8ff")
            .get().await()

        // Search by email prefix (case-insensitive)
        val emailSnapshot = db.collection("users")
            .whereGreaterThanOrEqualTo("email", q)
            .whereLessThanOrEqualTo("email", q + "\uf8ff")
            .get().await()

        // Merge & deduplicate
        val seen = mutableSetOf<String>()
        val results = mutableListOf<OnlineUser>()

        fun addUser(doc: com.google.firebase.firestore.DocumentSnapshot) {
            val uid = doc.id
            if (uid == currentUid || !seen.add(uid)) return
            results.add(
                OnlineUser(
                    uid = uid,
                    displayName = doc.getString("displayName") ?: "",
                    email = doc.getString("email") ?: "",
                    isOnline = doc.getBoolean("isOnline") ?: false
                )
            )
        }

        for (doc in nameSnapshot.documents) addUser(doc)
        for (doc in emailSnapshot.documents) addUser(doc)

        return results
    }

    suspend fun sendFriendRequest(toUid: String) {
        val fromUid = getCurrentUid() ?: return
        db.collection("friendRequests").document(toUid)
            .collection("received").document(fromUid)
            .set(mapOf("fromUid" to fromUid, "timestamp" to FieldValue.serverTimestamp()))
            .await()
    }

    suspend fun acceptFriendRequest(fromUid: String) {
        val uid = getCurrentUid() ?: return
        val batch = db.batch()
        val ts = FieldValue.serverTimestamp()
        batch.set(
            db.collection("friends").document(uid)
                .collection("friends").document(fromUid),
            mapOf("addedAt" to ts)
        )
        batch.set(
            db.collection("friends").document(fromUid)
                .collection("friends").document(uid),
            mapOf("addedAt" to ts)
        )
        batch.delete(
            db.collection("friendRequests").document(uid)
                .collection("received").document(fromUid)
        )
        batch.commit().await()
    }

    suspend fun rejectFriendRequest(fromUid: String) {
        val uid = getCurrentUid() ?: return
        db.collection("friendRequests").document(uid)
            .collection("received").document(fromUid)
            .delete().await()
    }

    suspend fun getFriends(): List<OnlineUser> {
        val uid = getCurrentUid() ?: return emptyList()
        val snapshot = db.collection("friends").document(uid)
            .collection("friends").get().await()
        if (snapshot.isEmpty) return emptyList()
        val friendIds = snapshot.documents.map { it.id }
        val userDocs = db.collection("users")
            .whereIn("__name__", friendIds).get().await()
        return userDocs.documents.map { doc ->
            OnlineUser(
                uid = doc.id,
                displayName = doc.getString("displayName") ?: "",
                email = doc.getString("email") ?: "",
                isOnline = doc.getBoolean("isOnline") ?: false
            )
        }
    }

    fun observeFriendRequests(): Flow<List<OnlineUser>> = callbackFlow {
        val uid = getCurrentUid()
        if (uid == null) {
            trySend(emptyList()); close(); return@callbackFlow
        }
        val listener = db.collection("friendRequests").document(uid)
            .collection("received")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing friend requests", error)
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener
                val fromIds = snapshot.documents.map { it.id }
                if (fromIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                db.collection("users")
                    .whereIn("__name__", fromIds)
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        val users = userSnapshot.documents.map { doc ->
                            OnlineUser(
                                uid = doc.id,
                                displayName = doc.getString("displayName") ?: "",
                                email = doc.getString("email") ?: "",
                                isOnline = doc.getBoolean("isOnline") ?: false
                            )
                        }
                        trySend(users)
                    }
            }
        awaitClose { listener.remove() }
    }

    fun observeFriends(): Flow<List<OnlineUser>> = callbackFlow {
        val uid = getCurrentUid()
        if (uid == null) {
            trySend(emptyList()); close(); return@callbackFlow
        }
        val listener = db.collection("friends").document(uid)
            .collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { return@addSnapshotListener }
                if (snapshot == null) return@addSnapshotListener
                val friendIds = snapshot.documents.map { it.id }
                if (friendIds.isEmpty()) { trySend(emptyList()); return@addSnapshotListener }
                db.collection("users")
                    .whereIn("__name__", friendIds)
                    .get()
                    .addOnSuccessListener { userSnapshot ->
                        val users = userSnapshot.documents.map { doc ->
                            OnlineUser(
                                uid = doc.id,
                                displayName = doc.getString("displayName") ?: "",
                                email = doc.getString("email") ?: "",
                                isOnline = doc.getBoolean("isOnline") ?: false
                            )
                        }
                        trySend(users)
                    }
            }
        awaitClose { listener.remove() }
    }

    suspend fun removeFriend(friendUid: String) {
        val uid = getCurrentUid() ?: return
        db.collection("friends").document(uid)
            .collection("friends").document(friendUid).delete().await()
        db.collection("friends").document(friendUid)
            .collection("friends").document(uid).delete().await()
    }

    suspend fun createMatch(opponentUid: String, opponentName: String): String? {
        val uid = getCurrentUid() ?: return null
        val userName = getUserDisplayName(uid) ?: "Jugador"
        val matchData = mapOf(
            "playerX" to uid,
            "playerO" to opponentUid,
            "playerXName" to userName,
            "playerOName" to opponentName,
            "participants" to listOf(uid, opponentUid),
            "board" to List(9) { "" },
            "currentPlayer" to "X",
            "status" to "playing",
            "winner" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "lastMoveAt" to FieldValue.serverTimestamp()
        )
        val docRef = db.collection("matches").add(matchData).await()
        return docRef.id
    }

    fun observeMatch(matchId: String): Flow<GameMatch?> = callbackFlow {
        val listener = db.collection("matches").document(matchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(null); return@addSnapshotListener }
                if (snapshot == null || !snapshot.exists()) { trySend(null); return@addSnapshotListener }
                trySend(snapshotToMatch(snapshot))
            }
        awaitClose { listener.remove() }
    }

    suspend fun makeMove(matchId: String, index: Int, symbol: String): Boolean = runCatching {
        val matchRef = db.collection("matches").document(matchId)
        val match = matchRef.get().await()
        val board = match.get("board") as? List<String> ?: return@runCatching false
        val currentPlayer = match.getString("currentPlayer") ?: return@runCatching false
        if (currentPlayer != symbol) return@runCatching false
        if (board[index].isNotEmpty()) return@runCatching false

        val updatedBoard = board.toMutableList()
        updatedBoard[index] = symbol

        val nextPlayer = if (symbol == "X") "O" else "X"

        // Check win
        val winCombos = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        var winner: String? = null
        var isDraw = false
        for (combo in winCombos) {
            if (updatedBoard[combo[0]] == symbol && updatedBoard[combo[1]] == symbol && updatedBoard[combo[2]] == symbol) {
                winner = symbol
                break
            }
        }
        if (winner == null && !updatedBoard.contains("")) {
            isDraw = true
        }

        val updates = mutableMapOf<String, Any>(
            "board" to updatedBoard,
            "currentPlayer" to nextPlayer,
            "lastMoveAt" to FieldValue.serverTimestamp()
        )
        if (winner != null) {
            updates["status"] = "finished"
            updates["winner"] = winner
        } else if (isDraw) {
            updates["status"] = "finished"
            updates["winner"] = "draw"
        }

        matchRef.update(updates).await()
        true
    }.getOrDefault(false)

    suspend fun getUserDisplayName(uid: String): String? {
        val doc = db.collection("users").document(uid).get().await()
        return doc.getString("displayName")
    }

    suspend fun removeMatch(matchId: String) {
        db.collection("matches").document(matchId).delete().await()
    }

    fun observeMyActiveMatches(): Flow<List<GameMatch>> = callbackFlow {
        val uid = getCurrentUid()
        if (uid == null) {
            trySend(emptyList()); close(); return@callbackFlow
        }
        val listener = db.collection("matches")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { return@addSnapshotListener }
                if (snapshot == null) { return@addSnapshotListener }
                val matches = snapshot.documents
                    .mapNotNull { doc ->
                        val status = doc.getString("status")
                        if (status == "playing" || status == "waiting") {
                            snapshotToMatch(doc)
                        } else null
                    }
                trySend(matches)
            }
        awaitClose { listener.remove() }
    }

    // ─── Sync: Game Stats ─────────────────────────────────────────
    suspend fun syncStats(stats: GameStatsEntity) {
        val uid = getCurrentUid() ?: return
        db.collection("users").document(uid)
            .collection("stats").document(stats.modeId)
            .set(stats).await()
    }

    suspend fun syncAllStats(statsList: List<GameStatsEntity>) {
        val uid = getCurrentUid() ?: return
        val batch = db.batch()
        val userRef = db.collection("users").document(uid)
        for (stats in statsList) {
            batch.set(userRef.collection("stats").document(stats.modeId), stats)
        }
        batch.commit().await()
    }

    suspend fun loadStatsFromFirebase(): List<GameStatsEntity> {
        val uid = getCurrentUid() ?: return emptyList()
        val snapshot = db.collection("users").document(uid)
            .collection("stats").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(GameStatsEntity::class.java)?.copy(modeId = doc.id)
        }
    }

    // ─── Sync: Cosmetics ──────────────────────────────────────────
    suspend fun syncCosmetic(cosmetic: UnlockedCosmeticEntity) {
        val uid = getCurrentUid() ?: return
        db.collection("users").document(uid)
            .collection("cosmetics").document(cosmetic.cosmeticId)
            .set(mapOf("isUnlocked" to cosmetic.isUnlocked)).await()
    }

    suspend fun syncAllCosmetics(cosmetics: List<UnlockedCosmeticEntity>) {
        val uid = getCurrentUid() ?: return
        val batch = db.batch()
        val userRef = db.collection("users").document(uid)
        for (item in cosmetics) {
            batch.set(userRef.collection("cosmetics").document(item.cosmeticId),
                mapOf("isUnlocked" to item.isUnlocked))
        }
        batch.commit().await()
    }

    suspend fun loadCosmeticsFromFirebase(): Map<String, Boolean> {
        val uid = getCurrentUid() ?: return emptyMap()
        val snapshot = db.collection("users").document(uid)
            .collection("cosmetics").get().await()
        return snapshot.documents.associate { doc ->
            doc.id to (doc.getBoolean("isUnlocked") ?: false)
        }
    }

    // ─── Sync: Achievements ───────────────────────────────────────
    suspend fun syncAchievement(achievement: AchievementEntity) {
        val uid = getCurrentUid() ?: return
        db.collection("users").document(uid)
            .collection("achievements").document(achievement.achievementId)
            .set(achievement).await()
    }

    suspend fun syncAllAchievements(achievements: List<AchievementEntity>) {
        val uid = getCurrentUid() ?: return
        val batch = db.batch()
        val userRef = db.collection("users").document(uid)
        for (ach in achievements) {
            batch.set(userRef.collection("achievements").document(ach.achievementId), ach)
        }
        batch.commit().await()
    }

    suspend fun loadAchievementsFromFirebase(): List<AchievementEntity> {
        val uid = getCurrentUid() ?: return emptyList()
        val snapshot = db.collection("users").document(uid)
            .collection("achievements").get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(AchievementEntity::class.java)?.copy(achievementId = doc.id)
        }
    }

    // ─── Sync: Settings ───────────────────────────────────────────
    suspend fun syncSettings(settings: UserSettingsEntity) {
        val uid = getCurrentUid() ?: return
        db.collection("users").document(uid)
            .collection("settings").document("singleton")
            .set(settings).await()
    }

    suspend fun loadSettingsFromFirebase(): UserSettingsEntity? {
        val uid = getCurrentUid() ?: return null
        val doc = db.collection("users").document(uid)
            .collection("settings").document("singleton").get().await()
        return doc.toObject(UserSettingsEntity::class.java)
    }

    // ─── Bulk sync: push all local data to Firebase ───────────────
    suspend fun pushAllToFirebase(repository: GameRepository) {
        val allStats = repository.allStats.first()
        val allCosmetics = repository.allCosmetics.first()
        val allAchievements = repository.allAchievements.first()
        val settings = repository.settings.first()

        if (allStats.isNotEmpty()) syncAllStats(allStats)
        if (allCosmetics.isNotEmpty()) syncAllCosmetics(allCosmetics)
        if (allAchievements.isNotEmpty()) syncAllAchievements(allAchievements)
        if (settings != null) syncSettings(settings)
    }

    // ─── Bulk sync: pull all Firebase data into Room ──────────────
    suspend fun pullAllFromFirebase(repository: GameRepository) {
        val cloudStats = loadStatsFromFirebase()
        for (stats in cloudStats) {
            repository.insertOrUpdateStats(stats)
        }
        val cloudCosmetics = loadCosmeticsFromFirebase()
        for ((id, unlocked) in cloudCosmetics) {
            if (unlocked) repository.unlockCosmetic(id)
        }
        val cloudAchievements = loadAchievementsFromFirebase()
        for (ach in cloudAchievements) {
            repository.insertAchievement(ach)
        }
        val cloudSettings = loadSettingsFromFirebase()
        if (cloudSettings != null) {
            repository.saveSettings(cloudSettings)
        }
    }

    private fun snapshotToMatch(snapshot: DocumentSnapshot): GameMatch {
        return GameMatch(
            id = snapshot.id,
            playerX = snapshot.getString("playerX") ?: "",
            playerO = snapshot.getString("playerO") ?: "",
            playerXName = snapshot.getString("playerXName") ?: "",
            playerOName = snapshot.getString("playerOName") ?: "",
            board = (snapshot.get("board") as? List<String>) ?: List(9) { "" },
            currentPlayer = snapshot.getString("currentPlayer") ?: "X",
            status = snapshot.getString("status") ?: "waiting",
            winner = snapshot.getString("winner")
        )
    }
}
