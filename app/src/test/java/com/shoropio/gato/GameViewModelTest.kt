package com.shoropio.gato

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.shoropio.gato.audio.SoundSynthesizer
import com.shoropio.gato.data.GameDatabase
import com.shoropio.gato.data.GameRepository
import com.shoropio.gato.viewmodel.GameViewModel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameViewModelTest {
    private lateinit var database: GameDatabase
    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        SoundSynthesizer.isSoundEnabled = false
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        viewModel = GameViewModel(GameRepository(database.gameDao()))
    }

    @After
    fun tearDown() {
        database.close()
        SoundSynthesizer.isSoundEnabled = true
    }

    @Test
    fun `pvp alternates turns after every valid move`() = runTest {
        viewModel.startNewSession("pvp")

        viewModel.onCellClicked(0)
        assertEquals("O", viewModel.currentPlayer.value)

        viewModel.onCellClicked(1)
        assertEquals("X", viewModel.currentPlayer.value)

        viewModel.onCellClicked(2)
        assertEquals("O", viewModel.currentPlayer.value)

        assertArrayEquals(
            arrayOf("X", "O", "X", "", "", "", "", "", ""),
            viewModel.board.value
        )
    }
}
