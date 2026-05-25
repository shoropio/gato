package com.shoropio.gato.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shoropio.gato.audio.SoundSynthesizer
import com.shoropio.gato.data.AchievementEntity
import com.shoropio.gato.data.GameDatabase
import com.shoropio.gato.data.GameRepository
import com.shoropio.gato.data.GameResult
import com.shoropio.gato.data.GameStatsEntity
import com.shoropio.gato.data.UserSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Representation of the game board match result.
 */
sealed class GamePlayState {
    object Idle : GamePlayState()
    object Active : GamePlayState()
    data class Won(val winner: String, val winningLine: List<Int>) : GamePlayState()
    object Draw : GamePlayState()
    object DemoRunning : GamePlayState()
}

class GameViewModel(val repository: GameRepository) : ViewModel() {

    // 1. Game State Streams
    private val _board = MutableStateFlow(Array(9) { "" })
    val board: StateFlow<Array<String>> = _board.asStateFlow()

    private val _currentPlayer = MutableStateFlow("X")
    val currentPlayer: StateFlow<String> = _currentPlayer.asStateFlow()

    private val _gamePlayState = MutableStateFlow<GamePlayState>(GamePlayState.Idle)
    val gamePlayState: StateFlow<GamePlayState> = _gamePlayState.asStateFlow()

    private val _gameMode = MutableStateFlow("vs_ai") // "pvp", "vs_ai", "demo"
    val gameMode: StateFlow<String> = _gameMode.asStateFlow()

    private val _aiDifficulty = MutableStateFlow("normal") // "easy", "normal", "hard", "impossible"
    val aiDifficulty: StateFlow<String> = _aiDifficulty.asStateFlow()

    // Real-time scoreboard for the active session (resets on mode change)
    private val _p1Score = MutableStateFlow(0)
    val p1Score: StateFlow<Int> = _p1Score.asStateFlow()

    private val _p2Score = MutableStateFlow(0)
    val p2Score: StateFlow<Int> = _p2Score.asStateFlow()

    private val _drawScore = MutableStateFlow(0)
    val drawScore: StateFlow<Int> = _drawScore.asStateFlow()

    // 2. Database Streams
    val allStats: StateFlow<List<GameStatsEntity>> = repository.allStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<UserSettingsEntity?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allAchievements: StateFlow<List<AchievementEntity>> = repository.allAchievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCosmetics = repository.allCosmetics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Game Animations & Visual Effects state
    private val _particleTrigger = MutableSharedFlow<List<Int>>() // indices of winner line
    val particleTrigger: SharedFlow<List<Int>> = _particleTrigger.asSharedFlow()

    // 4. Demo (AI vs AI) controller
    private var demoJob: Job? = null

    init {
        // Observe settings changes and synchronize sound synthesizer state
        viewModelScope.launch {
            repository.settings.collect { userSettings ->
                userSettings?.let {
                    SoundSynthesizer.isSoundEnabled = it.soundOn
                    if (it.soundOn) {
                        SoundSynthesizer.startAmbientMusic()
                    } else {
                        SoundSynthesizer.stopAmbientMusic()
                    }
                }
            }
        }
    }

    /**
     * Set the current Game Mode and reset scores for the session.
     */
    fun startNewSession(mode: String, difficulty: String = "normal") {
        _gameMode.value = mode
        _aiDifficulty.value = difficulty
        _p1Score.value = 0
        _p2Score.value = 0
        _drawScore.value = 0
        resetBoard()
        
        if (mode == "demo") {
            startDemoPlay()
        } else {
            stopDemoPlay()
        }
    }

    /**
     * Resets board cells and makes starting turn "X".
     */
    fun resetBoard() {
        stopDemoPlay()
        _board.value = Array(9) { "" }
        _currentPlayer.value = "X"
        _gamePlayState.value = GamePlayState.Active
        
        if (_gameMode.value == "demo") {
            startDemoPlay()
        } else if (_gameMode.value == "vs_ai" && _currentPlayer.value == "O") {
            // Trigger AI move if somehow AI starts
            triggerAiMoveIfNeeded()
        }
    }

    /**
     * Triggered when a cell is clicked by a human player.
     */
    fun onCellClicked(index: Int) {
        if (_gamePlayState.value != GamePlayState.Active) return
        if (_gameMode.value == "demo") return // Screen interactions disabled during demo
        if (_board.value[index].isNotEmpty()) return

        // 1. Play feedback sound
        SoundSynthesizer.playClick()

        // 2. Register Player Move
        val playerSymbol = _currentPlayer.value
        val updatedBoard = _board.value.clone().toMutableList()
        updatedBoard[index] = playerSymbol
        _board.value = updatedBoard.toTypedArray()

        // 3. Evaluate Board State
        if (checkWinResult(playerSymbol)) return

        if (checkDrawResult()) return

        // 4. Set Turn to Opponent
        _currentPlayer.value = "O"

        // 5. If Player vs AI, immediately request AI play
        if (_gameMode.value == "vs_ai") {
            triggerAiMoveIfNeeded()
        }
    }

    private fun triggerAiMoveIfNeeded() {
        if (_gamePlayState.value != GamePlayState.Active) return
        if (_currentPlayer.value != "O") return

        viewModelScope.launch {
            _gamePlayState.value = GamePlayState.DemoRunning // Temporarily block overlay click
            delay(580) // AI mental delay for realistic immersion
            
            val aiSymbol = "O"
            val humanSymbol = "X"
            val bestMove = calculateAiMove(_board.value, _aiDifficulty.value, aiSymbol, humanSymbol)
            
            if (bestMove != -1) {
                val updatedBoard = _board.value.clone().toMutableList()
                updatedBoard[bestMove] = aiSymbol
                _board.value = updatedBoard.toTypedArray()
                
                // Play click
                SoundSynthesizer.playClick()
                
                _gamePlayState.value = GamePlayState.Active // Unlock
                if (!checkWinResult(aiSymbol)) {
                    if (!checkDrawResult()) {
                        _currentPlayer.value = "X"
                    }
                }
            } else {
                _gamePlayState.value = GamePlayState.Active
            }
        }
    }

    /**
     * Starts the automated AI vs AI demonstration duel.
     */
    private fun startDemoPlay() {
        stopDemoPlay()
        _gamePlayState.value = GamePlayState.Active
        
        demoJob = viewModelScope.launch {
            delay(800) // Initial setup delay
            while (isActive && _gamePlayState.value == GamePlayState.Active) {
                val activeSymbol = _currentPlayer.value
                val opponentSymbol = if (activeSymbol == "X") "O" else "X"
                
                // One AI plays (X uses Normal/Hard, O uses Impossible for fun fights!)
                val sideDifficulty = if (activeSymbol == "X") "hard" else "impossible"
                val moveIndex = calculateAiMove(_board.value, sideDifficulty, activeSymbol, opponentSymbol)
                
                if (moveIndex != -1) {
                    val updatedBoard = _board.value.clone().toMutableList()
                    updatedBoard[moveIndex] = activeSymbol
                    _board.value = updatedBoard.toTypedArray()
                    
                    SoundSynthesizer.playGlitchBeep()
                    
                    if (checkWinResult(activeSymbol)) break
                    if (checkDrawResult()) break
                    
                    _currentPlayer.value = opponentSymbol
                } else {
                    break
                }
                
                delay(1200) // Pacing delay to follow the cosmic match
            }
        }
    }

    private fun stopDemoPlay() {
        demoJob?.cancel()
        demoJob = null
    }

    /**
     * Checks if a player has won the game.
     */
    private fun checkWinResult(symbol: String): Boolean {
        val boardArr = _board.value
        val winningCombinations = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // Rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // Columns
            listOf(0, 4, 8), listOf(2, 4, 6)                  // Diagonals
        )

        for (combo in winningCombinations) {
            if (boardArr[combo[0]] == symbol && boardArr[combo[1]] == symbol && boardArr[combo[2]] == symbol) {
                val state = GamePlayState.Won(symbol, combo)
                _gamePlayState.value = state
                stopDemoPlay()

                // Update Session scores
                if (symbol == "X") {
                    _p1Score.value += 1
                } else {
                    _p2Score.value += 1
                }

                // Play Victory synthesizers call
                SoundSynthesizer.playVictory()
                
                // Emit particles
                viewModelScope.launch {
                    _particleTrigger.emit(combo)
                }

                // Commit statistics to SQLite in the background
                viewModelScope.launch(Dispatchers.IO) {
                    val modeKey = getStorageModeKey()
                    val result = if (_gameMode.value == "pvp") {
                        if (symbol == "X") GameResult.WIN else GameResult.LOSS
                    } else if (_gameMode.value == "demo") {
                        GameResult.WIN // demo matches triggers watcher stats
                    } else {
                        // Player vs AI
                        if (symbol == "O") GameResult.LOSS else GameResult.WIN
                    }
                    repository.recordGameResult(modeKey, result)
                }
                return true
            }
        }
        return false
    }

    private fun checkDrawResult(): Boolean {
        if (!_board.value.contains("")) {
            _gamePlayState.value = GamePlayState.Draw
            stopDemoPlay()
            
            _drawScore.value += 1
            
            SoundSynthesizer.playDraw()

            viewModelScope.launch(Dispatchers.IO) {
                repository.recordGameResult(getStorageModeKey(), GameResult.DRAW)
            }
            return true
        }
        return false
    }

    private fun getStorageModeKey(): String {
        return when (_gameMode.value) {
            "pvp" -> "pvp"
            "demo" -> "demo"
            else -> "vs_ai_${_aiDifficulty.value}"
        }
    }

    /**
     * AI Core logic controller.
     */
    private fun calculateAiMove(
        board: Array<String>,
        difficulty: String,
        aiSymbol: String,
        humanSymbol: String
    ): Int {
        val emptyIndices = board.indices.filter { board[it].isEmpty() }
        if (emptyIndices.isEmpty()) return -1

        return when (difficulty) {
            "easy" -> {
                // Play fully random move
                emptyIndices.random()
            }
            "normal" -> {
                // 50% chance block/win, 50% random
                if (Random.nextFloat() < 0.5f) {
                    val smartMove = findWinningOrBlockingMove(board, aiSymbol, humanSymbol)
                    if (smartMove != -1) smartMove else emptyIndices.random()
                } else {
                    emptyIndices.random()
                }
            }
            "hard" -> {
                // Heuristic strategy: check if I can win -> if human can win -> center -> corners -> random
                val winMove = findImmediateWin(board, aiSymbol)
                if (winMove != -1) return winMove

                val blockMove = findImmediateWin(board, humanSymbol)
                if (blockMove != -1) return blockMove

                if (board[4].isEmpty()) return 4

                val corners = listOf(0, 2, 6, 8).filter { board[it].isEmpty() }
                if (corners.isNotEmpty()) return corners.random()

                emptyIndices.random()
            }
            "impossible" -> {
                // Minimax algorithm optimal calculation
                getBestMoveMinimax(board, aiSymbol, humanSymbol)
            }
            else -> emptyIndices.random()
        }
    }

    private fun findWinningOrBlockingMove(board: Array<String>, aiSymbol: String, humanSymbol: String): Int {
        val winMove = findImmediateWin(board, aiSymbol)
        if (winMove != -1) return winMove
        val blockMove = findImmediateWin(board, humanSymbol)
        if (blockMove != -1) return blockMove
        return -1
    }

    private fun findImmediateWin(board: Array<String>, symbol: String): Int {
        val winCombos = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (combo in winCombos) {
            val cell1 = board[combo[0]]
            val cell2 = board[combo[1]]
            val cell3 = board[combo[2]]

            if (cell1 == symbol && cell2 == symbol && cell3.isEmpty()) return combo[2]
            if (cell1 == symbol && cell2.isEmpty() && cell3 == symbol) return combo[1]
            if (cell1.isEmpty() && cell2 == symbol && cell3 == symbol) return combo[0]
        }
        return -1
    }

    /**
     * Minimax Entry Point
     */
    private fun getBestMoveMinimax(board: Array<String>, aiSymbol: String, humanSymbol: String): Int {
        var bestVal = -1000
        var bestMove = -1

        for (i in 0..8) {
            if (board[i].isEmpty()) {
                val tempBoard = board.clone()
                tempBoard[i] = aiSymbol
                
                val moveVal = minimax(tempBoard, 0, false, aiSymbol, humanSymbol)
                
                if (moveVal > bestVal) {
                    bestMove = i
                    bestVal = moveVal
                }
            }
        }
        return bestMove
    }

    private fun minimax(
        board: Array<String>,
        depth: Int,
        isMax: Boolean,
        aiSymbol: String,
        humanSymbol: String
    ): Int {
        val score = evaluateBoardScore(board, aiSymbol, humanSymbol)

        // Base cases
        if (score == 10) return score - depth
        if (score == -10) return score + depth
        if (!board.contains("")) return 0

        if (isMax) {
            var best = -1000
            for (i in 0..8) {
                if (board[i].isEmpty()) {
                    val tempBoard = board.clone()
                    tempBoard[i] = aiSymbol
                    best = maxOf(best, minimax(tempBoard, depth + 1, false, aiSymbol, humanSymbol))
                }
            }
            return best
        } else {
            var best = 1000
            for (i in 0..8) {
                if (board[i].isEmpty()) {
                    val tempBoard = board.clone()
                    tempBoard[i] = humanSymbol
                    best = minOf(best, minimax(tempBoard, depth + 1, true, aiSymbol, humanSymbol))
                }
            }
            return best
        }
    }

    private fun evaluateBoardScore(board: Array<String>, aiSymbol: String, humanSymbol: String): Int {
        val winCombos = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (combo in winCombos) {
            if (board[combo[0]] == aiSymbol && board[combo[1]] == aiSymbol && board[combo[2]] == aiSymbol) {
                return 10
            }
            if (board[combo[0]] == humanSymbol && board[combo[1]] == humanSymbol && board[combo[2]] == humanSymbol) {
                return -10
            }
        }
        return 0
    }

    // 5. User Settings actions
    fun toggleSound() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = getSettings() ?: UserSettingsEntity()
            val updated = current.copy(soundOn = !current.soundOn)
            repository.saveSettings(updated)
        }
    }

    fun toggleVibration() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = getSettings() ?: UserSettingsEntity()
            val updated = current.copy(vibrationOn = !current.vibrationOn)
            repository.saveSettings(updated)
        }
    }

    fun setBoardStyle(style: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = getSettings() ?: UserSettingsEntity()
            val updated = current.copy(boardStyle = style)
            repository.saveSettings(updated)
        }
    }

    fun setAvatar(avatar: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = getSettings() ?: UserSettingsEntity()
            val updated = current.copy(selectedAvatar = avatar)
            repository.saveSettings(updated)
        }
    }

    suspend fun getSettings(): UserSettingsEntity? {
        return repository.getSettings()
    }

    override fun onCleared() {
        super.onCleared()
        stopDemoPlay()
        SoundSynthesizer.stopAmbientMusic()
    }

    companion object {
        fun createFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = GameDatabase.getDatabase(context.applicationContext, CoroutineScope(Dispatchers.IO))
                val repo = GameRepository(db.gameDao())
                return GameViewModel(repo) as T
            }
        }
    }
}
