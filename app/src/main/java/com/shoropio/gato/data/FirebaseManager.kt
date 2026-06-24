package com.shoropio.gato.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.shoropio.gato.notification.GatoMessagingService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Suppress("UNCHECKED_CAST")

data class OnlineUser(
    val uid: String = "",
    val displayName: String = "",
    val isOnline: Boolean = false
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
    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore

    suspend fun signInAnonymously(): Result<String> = runCatching {
        val result = auth.signInAnonymously().await()
        result.user?.uid ?: throw Exception("No se pudo autenticar")
    }

    fun getCurrentUid(): String? = auth.currentUser?.uid

    suspend fun createUserProfile(displayName: String) {
        val uid = getCurrentUid() ?: return
        val token = GatoMessagingService.fcmToken
        val user = mutableMapOf<String, Any>(
            "displayName" to displayName,
            "isOnline" to true,
            "lastSeen" to FieldValue.serverTimestamp()
        )
        if (token != null) {
            user["fcmToken"] = token
        }
        db.collection("users").document(uid).set(user).await()
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
        val snapshot = db.collection("users")
            .whereGreaterThanOrEqualTo("displayName", query)
            .whereLessThanOrEqualTo("displayName", query + "\uf8ff")
            .get().await()
        val currentUid = getCurrentUid()
        return snapshot.documents.mapNotNull { doc ->
            val uid = doc.id
            if (uid == currentUid) return@mapNotNull null
            OnlineUser(
                uid = uid,
                displayName = doc.getString("displayName") ?: "",
                isOnline = doc.getBoolean("isOnline") ?: false
            )
        }
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
        // Add to both users' friends subcollections
        db.collection("friends").document(uid)
            .collection("friends").document(fromUid)
            .set(mapOf("addedAt" to FieldValue.serverTimestamp())).await()
        db.collection("friends").document(fromUid)
            .collection("friends").document(uid)
            .set(mapOf("addedAt" to FieldValue.serverTimestamp())).await()
        // Remove the request
        db.collection("friendRequests").document(uid)
            .collection("received").document(fromUid)
            .delete().await()
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
