package com.turkce.kelimesolitaire.presentation.util

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.Executors

object GameSettingsManager {

    private const val PREFS_NAME = "kelime_solitaire_prefs"
    private const val KEY_SOUND_ENABLED = "key_sound_enabled"
    private const val KEY_HAPTIC_ENABLED = "key_haptic_enabled"

    private val audioExecutor = Executors.newSingleThreadExecutor()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Sound Settings ---

    fun isSoundEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    // --- Haptic Settings ---

    fun isHapticEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_HAPTIC_ENABLED, true)
    }

    fun setHapticEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
    }

    // --- Vibration Dispatcher ---

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Very subtle, crisp tap for UI button clicks or card pick-up.
     */
    fun vibrateLight(context: Context) {
        if (!isHapticEnabled(context)) return
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(18)
            }
        } catch (_: Throwable) {}
    }

    /**
     * Pleasant solid snap when card is placed on a category or column.
     */
    fun vibrateCardSnap(context: Context) {
        if (!isHapticEnabled(context)) return
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(32, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(32)
            }
        } catch (_: Throwable) {}
    }

    /**
     * Celebratory double pulse when category is completed or level won.
     */
    fun vibrateSuccess(context: Context) {
        if (!isHapticEnabled(context)) return
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 35, 60, 50)
                val amplitudes = intArrayOf(0, 180, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 35, 60, 50), -1)
            }
        } catch (_: Throwable) {}
    }

    // --- Audio Synthesis ---

    /**
     * Crisp, soft card placement / snap sound.
     */
    fun playCardSnapSound(context: Context) {
        if (!isSoundEnabled(context)) return
        audioExecutor.execute {
            try {
                val sampleRate = 44100
                val durationMs = 45
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    // Pitch sweeps down quickly from 520Hz to 180Hz (wood/card tap sound)
                    val freq = 520.0 - (progress * 340.0)
                    val decay = Math.exp(-progress * 7.0) // sharp natural decay
                    sample[i] = Math.sin(2.0 * Math.PI * i * freq / sampleRate) * decay * 0.35
                }

                var idx = 0
                for (dVal in sample) {
                    val valShort = (dVal * 32767).toInt().coerceIn(-32768, 32767).toShort()
                    generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
                }

                playPcmBytes(generatedSnd, sampleRate, durationMs + 20)
            } catch (_: Throwable) {}
        }
    }

    /**
     * Cheerful two-tone coin chime.
     */
    fun playCoinSound(context: Context) {
        if (!isSoundEnabled(context)) return
        audioExecutor.execute {
            try {
                val sampleRate = 44100
                val durationMs = 200
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                for (i in 0 until numSamples) {
                    val freq = if (i < sampleRate * 0.06) 987.77 else 1318.51
                    val fadePercent = if (i > sampleRate * 0.10) {
                        maxOf(0.0, 1.0 - ((i - sampleRate * 0.10) / (sampleRate * 0.10)))
                    } else {
                        1.0
                    }
                    sample[i] = Math.sin(2.0 * Math.PI * i / (sampleRate / freq)) * fadePercent * 0.4
                }

                var idx = 0
                for (dVal in sample) {
                    val valShort = (dVal * 32767).toInt().coerceIn(-32768, 32767).toShort()
                    generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
                }

                playPcmBytes(generatedSnd, sampleRate, durationMs + 30)
            } catch (_: Throwable) {}
        }
    }

    /**
     * Soft UI click/pop sound.
     */
    fun playButtonClickSound(context: Context) {
        if (!isSoundEnabled(context)) return
        audioExecutor.execute {
            try {
                val sampleRate = 44100
                val durationMs = 30
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = 880.0 - (progress * 440.0)
                    val decay = Math.exp(-progress * 8.0)
                    sample[i] = Math.sin(2.0 * Math.PI * i * freq / sampleRate) * decay * 0.25
                }

                var idx = 0
                for (dVal in sample) {
                    val valShort = (dVal * 32767).toInt().coerceIn(-32768, 32767).toShort()
                    generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                    generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
                }

                playPcmBytes(generatedSnd, sampleRate, durationMs + 20)
            } catch (_: Throwable) {}
        }
    }

    private fun playPcmBytes(generatedSnd: ByteArray, sampleRate: Int, sleepMs: Int) {
        var track: AudioTrack? = null
        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(generatedSnd, 0, generatedSnd.size)
            track.play()
            Thread.sleep(sleepMs.toLong())
        } catch (_: Throwable) {
        } finally {
            try {
                track?.release()
            } catch (_: Throwable) {}
        }
    }
}
