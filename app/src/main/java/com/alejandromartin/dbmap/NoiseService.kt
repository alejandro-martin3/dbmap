package com.alejandromartin.dbmap

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Servicio encargado de capturar audio desde el micrófono y calcular
 * el nivel aproximado de ruido ambiental en decibelios.
 * No graba, almacena ni transmite audio en ningún momento.
 */
class NoiseService {

    /**
     * Captura audio durante el tiempo indicado y devuelve una estimación
     * del nivel de ruido en decibelios (escala 0–120 dB).
     * El cálculo se basa en el valor RMS de la señal PCM convertido a dBFS
     * con un offset de calibración de +90 dB para aproximar dB SPL.
     *
     * @param durationMs Duración de la captura en milisegundos. Por defecto 3000 ms.
     * @return Nivel estimado de ruido en dB, o 0.0 si no se puede realizar la medición.
     */
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

        // Si el dispositivo no puede calcular el buffer mínimo, no se puede medir
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

            // Lee bloques de audio hasta que se cumple la duración indicada
            while (System.currentTimeMillis() < endTime) {
                val read = audioRecord.read(buffer, 0, buffer.size)

                if (read > 0) {
                    // Acumula el cuadrado de cada muestra para calcular el RMS
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
                    // Convierte RMS a dBFS y aplica offset para aproximar dB SPL
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