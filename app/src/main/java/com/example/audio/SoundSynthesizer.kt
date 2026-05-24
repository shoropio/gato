package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * A procedural retro synth sound engine for our Gato cyborg game.
 * Uses Android's low-level AudioTrack API to synthesize sine and square wave tones.
 * Completely standalone; requires no resource files.
 */
object SoundSynthesizer {
    private const val TAG = "SoundSynthesizer"
    private const val SAMPLE_RATE = 22050
    
    var isSoundEnabled: Boolean = true

    // Background ambient music control
    private var musicJob: Job? = null
    private val synthScope = CoroutineScope(Dispatchers.Default)

    /**
     * Synthesizes and plays a short frequency-swept sine tone.
     * Starts high and slides low, creating a futuristic interface tap.
     */
    fun playClick() {
        if (!isSoundEnabled) return
        synthScope.launch {
            try {
                val durationMs = 120
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                
                // Frequency sweep from 880Hz down to 445Hz
                val startFreq = 880.0
                val endFreq = 440.0
                
                var phase = 0.0
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * t
                    val angularFreq = 2.0 * Math.PI * currentFreq / SAMPLE_RATE
                    
                    // Sine wave with exponential volume decay
                    val envelope = Math.exp(-4.0 * t)
                    samples[i] = (sin(phase) * Short.MAX_VALUE * envelope * 0.45).toInt().toShort()
                    phase += angularFreq
                }
                
                playSamples(samples)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing click sound", e)
            }
        }
    }

    /**
     * Synthesizes a robotic/glitch action beep.
     */
    fun playGlitchBeep() {
        if (!isSoundEnabled) return
        synthScope.launch {
            try {
                val durationMs = 150
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                
                var phase = 0.0
                val freq = 1200.0
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val angularFreq = 2.0 * Math.PI * freq / SAMPLE_RATE
                    
                    // Square wave (creates retro pixel arcade sound)
                    val rawWave = if (sin(phase) >= 0) 1.0 else -1.0
                    // Staccato gateway gate envelope
                    val envelope = if (t < 0.2 || (t in 0.4..0.6) || t > 0.8) 0.3 else 0.0
                    
                    samples[i] = (rawWave * Short.MAX_VALUE * envelope * 0.3).toInt().toShort()
                    phase += angularFreq
                }
                
                playSamples(samples)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing glitch beep", e)
            }
        }
    }

    /**
     * Play a futuristic cybernetic sci-fi victory arpeggio in rapid tempo.
     */
    fun playVictory() {
        if (!isSoundEnabled) return
        synthScope.launch {
            try {
                // Frequencies for a retro gaming C Major 9 style synth arpeggio:
                // C5 (523Hz), E5 (659Hz), G5 (784Hz), B5 (987Hz), C6 (1046Hz)
                val freqs = listOf(523.25, 659.25, 783.99, 987.77, 1046.50)
                val noteDurationMs = 140
                
                for (freq in freqs) {
                    val numSamples = (SAMPLE_RATE * (noteDurationMs / 1000.0)).toInt()
                    val samples = ShortArray(numSamples)
                    var phase = 0.0
                    
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / numSamples
                        val angularFreq = 2.0 * Math.PI * freq / SAMPLE_RATE
                        val envelope = Math.exp(-2.5 * t)
                        samples[i] = (sin(phase) * Short.MAX_VALUE * envelope * 0.4).toInt().toShort()
                        phase += angularFreq
                    }
                    playSamples(samples)
                    delay(80) // Stagger notes
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing victory sound", e)
            }
        }
    }

    /**
     * Play a down-sliding low digital buzz to represent a draw or system shutdown.
     */
    fun playDraw() {
        if (!isSoundEnabled) return
        synthScope.launch {
            try {
                val durationMs = 400
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                
                val startFreq = 280.0
                val endFreq = 110.0
                var phase = 0.0
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val currentFreq = startFreq + (endFreq - startFreq) * t
                    val angularFreq = 2.0 * Math.PI * currentFreq / SAMPLE_RATE
                    
                    // Smooth linear envelope decay
                    val envelope = 1.0 - t
                    // Triangle or hybrid wave
                    val sineVal = sin(phase)
                    val waveVal = if (sineVal >= 0) 0.6 else -0.6
                    
                    samples[i] = (waveVal * Short.MAX_VALUE * envelope * 0.35).toInt().toShort()
                    phase += angularFreq
                }
                
                playSamples(samples)
            } catch (e: Exception) {
                Log.e(TAG, "Error playing draw sound", e)
            }
        }
    }

    /**
     * Starts continuous synthesized background space music.
     * Generates extremely soft ambient periodic cosmic tones in a loop.
     */
    fun startAmbientMusic() {
        if (!isSoundEnabled) {
            stopAmbientMusic()
            return
        }
        if (musicJob != null && musicJob?.isActive == true) return

        musicJob = synthScope.launch {
            // Ambient pentatonic sequence
            // E4 (329.63Hz), G#4 (415.30Hz), B4 (493.88Hz), C#5 (554.37Hz), E5 (659.25Hz)
            val baseNotes = listOf(329.63, 415.30, 493.88, 554.37, 659.25)
            
            while (isActive) {
                if (!isSoundEnabled) {
                    delay(2000)
                    continue
                }
                
                // Pick a note randomly to play a dreamy spaced-out melody
                val noteFreq = baseNotes.random()
                // Ambient notes have gentle long fades (e.g., 2.5 seconds)
                val durationMs = 2000
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                val samples = ShortArray(numSamples)
                var phase = 0.0
                
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val angularFreq = 2.0 * Math.PI * noteFreq / SAMPLE_RATE
                    
                    // Smooth attack and slow decay envelope (dream space pad)
                    val envelope = if (t < 0.25) {
                        t / 0.25 // Linear fade-in
                    } else {
                        Math.exp(-2.5 * (t - 0.25)) // Exponential fade-out
                    }
                    
                    // Ultra-quiet ambient note (0.1 max amplitude)
                    samples[i] = (sin(phase) * Short.MAX_VALUE * envelope * 0.07).toInt().toShort()
                    phase += angularFreq
                }
                
                playSamples(samples)
                // Relax with asymmetric cosmic pauses between chimes
                delay((1800..3500).random().toLong())
            }
        }
    }

    /**
     * Terminate the ambient soundtrack loop.
     */
    fun stopAmbientMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    /**
     * Low-level helper to write short array bytes directly into AudioTrack.
     */
    private fun playSamples(samples: ShortArray) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        
        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(Math.max(minBufferSize, samples.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
        } else {
            @Suppress("DEPRECATION")
            AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
                Math.max(minBufferSize, samples.size * 2),
                AudioTrack.MODE_STATIC,
                0
            )
        }
        
        track.write(samples, 0, samples.size)
        track.play()
        
        // Clean up the track when finished playing
        synthScope.launch {
            // Estimate playing duration
            val sleepMs = ((samples.size.toDouble() / SAMPLE_RATE) * 1000).toLong() + 200
            delay(sleepMs)
            try {
                track.stop()
                track.release()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
