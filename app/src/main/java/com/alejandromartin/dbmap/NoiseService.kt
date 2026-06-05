package com.alejandromartin.dbmap

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.log10
import kotlin.math.sqrt

class NoiseService {

    @SuppressLint("MissingPermission")
    fun medirRuidoDurante(durationMs: Long = 3000): Double {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            channelConfig,
            audioFormat
        )

        if (minBufferSize <= 0) {
            return 0.0
        }

        val bufferSize = minBufferSize.coerceAtLeast(4096)
        val buffer = ShortArray(bufferSize)

        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        var sumSquares = 0.0
        var totalSamples = 0

        return try {
            audioRecord.startRecording()

            val endTime = System.currentTimeMillis() + durationMs

            while (System.currentTimeMillis() < endTime) {
                val read = audioRecord.read(buffer, 0, buffer.size)

                if (read > 0) {
                    for (i in 0 until read) {
                        val sample = buffer[i].toDouble()
                        sumSquares += sample * sample
                    }
                    totalSamples += read
                }
            }

            if (totalSamples == 0) {
                0.0
            } else {
                val rms = sqrt(sumSquares / totalSamples)

                if (rms <= 0.0) {
                    0.0
                } else {
                    val dbfs = 20 * log10(rms / Short.MAX_VALUE)
                    val estimatedDb = dbfs + 90.0

                    estimatedDb.coerceIn(0.0, 120.0)
                }
            }
        } catch (e: Exception) {
            0.0
        } finally {
            try {
                audioRecord.stop()
            } catch (_: Exception) {
            }

            audioRecord.release()
        }
    }
}