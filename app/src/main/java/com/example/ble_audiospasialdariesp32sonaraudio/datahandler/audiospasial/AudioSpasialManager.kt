package com.example.ble_audiospasialdariesp32sonaraudio.datahandler.audiospasial

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.ble_audiospasialdariesp32sonaraudio.R
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class AudioSpasialManager(
    context: Context
)
{


    private val soundPool: SoundPool
    private var buzzingSound: Int = 0
    private var pingSound: Int = 0
    private var streamId: Int = 0 // Menyimpan streamId untuk kontrol play/stop

    private var streamIdKiri: Int = 0 // Stream ID untuk telinga kiri
    private var streamIdKanan: Int = 0 // Stream ID untuk telinga kanan

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load the buzzing sound from the raw resource
        buzzingSound = soundPool.load(context, R.raw.buzzing_sound, 1)
        pingSound = soundPool.load(context, R.raw.sonar_ping_sound, 1)
    }

    fun getSoundToPlay(soundOption: String): Int {
        return when (soundOption) {
            "ping" -> pingSound
            "buzzing" -> buzzingSound
            else -> buzzingSound // Default sound in case of invalid input
        }
    }

    fun playBuzzingSound(
        xPos: Float,
        yPos: Float,
        jarakMaksimum: Int,
        soundOption: String
    ) {
        // Menghitung jarak dari pusat ke titik (xPos, yPos)
        val jari_jari = sqrt(xPos * xPos + yPos * yPos) // Jarak

        // Menghitung sudut θ dalam radian dan membatasi hanya untuk kuadran 1 dan 2
        var theta = atan2(yPos, xPos)

        // Membatasi theta hanya untuk kuadran 1 dan 2 (0 <= theta <= pi)
        if (theta < 0) {
            theta += PI.toFloat()
        }

        // Normalisasi jarak (semakin jauh jarak, semakin kecil volume)
        val volume = (1 - (jari_jari / jarakMaksimum)).coerceIn(0f, 1f)

        // Hitung panning berdasarkan sudut theta (kuadran 1 dan 2)
        val leftVolume = (theta / PI.toFloat()) * volume
        val rightVolume = (1 - theta / PI.toFloat()) * volume




        Log.d("SoundController", "xPos: $xPos, yPos: $yPos, theta: ${theta * 180/Math.PI.toFloat()}, volume: $volume, jarak: ${jari_jari/100}")

        // Get the sound to play based on soundOption
        val soundToPlay = getSoundToPlay(soundOption)

        if (streamId == 0) {
            // Jika belum diputar, mulai putar suara dan simpan streamId
            streamId = soundPool.play(soundToPlay, leftVolume, rightVolume, 1, -1, 1f)
        } else {
            // Jika suara sudah diputar, perbarui volume tanpa menghentikan suara
            soundPool.setVolume(streamId, leftVolume, rightVolume)
        }
    }

    fun iidCalculationVolume(
        sudutTheta: Int,
        jarakMaksimum: Float,
        jari_jari: Float
    ):Pair<Float,Float>{

        var xPosObjek =  jari_jari * cos(sudutTheta * (Math.PI.toFloat() / 180))
        var yPosObjek =  jari_jari * sin(sudutTheta * (Math.PI.toFloat() / 180))

        var _sudutTheta = atan2(yPosObjek, xPosObjek)

        // Normalisasi jarak (semakin jauh jarak, semakin kecil volume)
        val volume = (1 - (jari_jari / jarakMaksimum)).coerceIn(0f, 1f)


        // Membatasi theta hanya untuk kuadran 1 dan 2 (0 <= theta <= pi)
        if (_sudutTheta < 0) {
            _sudutTheta += PI.toFloat()
        }

        // Hitung panning berdasarkan sudut theta (kuadran 1 dan 2)
        val leftVolume = (_sudutTheta / PI.toFloat()) * volume
        val rightVolume = (1 - _sudutTheta / PI.toFloat()) * volume


        return Pair(leftVolume, rightVolume)
    }

    // Fungsi untuk menghentikan suara secara manual
    fun stopBuzzingSound() {
        if (streamId != 0) {
            soundPool.stop(streamId)
            streamId = 0 // Reset streamId setelah dihentikan
        }
    }

    // Ubah sudut theta ke kuadran 1 dan 4
    //perbedaan waktu kedatangan suara antara dua telinga (iitd)



    fun itdCalculation(
        sudutTheta : Int
    ):Float{
        var sudutDalamRadian:Float = sudutTheta * (Math.PI.toFloat() / 180f)
        // rata rata jarak antra dua telinga itu 20 cm. 34300 itu kecepatan suara
        return (20 * sin(sudutDalamRadian)) / 34300 * 1000
    }




    fun simulate3DAudioUsingITD(
        jari_jari:Float,
        jarakMaksimum: Int,
        sudutTheta: Int,
        soundOption: String
    ){
        // Normalisasi jarak (semakin jauh jarak, semakin kecil volume)
        val volume = (1 - (jari_jari / jarakMaksimum)).coerceIn(0f, 1f)

        val(iidKiri, iidKanan) = iidCalculationVolume(sudutTheta, jarakMaksimum.toFloat(), jari_jari)
        val itdVolume = itdCalculation(sudutTheta)




        Log.d("SoundController", "ITD: $itdVolume ms for angle: $sudutTheta")

        // Get the sound to play based on soundOption
        val soundToPlay = getSoundToPlay(soundOption)

        if (streamIdKiri == 0 && streamIdKanan == 0) {
            when {
                sudutTheta > 90 -> {
                    // Kuadran 2: Suara kiri dulu, kanan dengan delay ITD
                    streamIdKiri = soundPool.play(soundToPlay, iidKiri, 0f, 1, -1, 1f)
                    Handler(Looper.getMainLooper()).postDelayed({
                        streamIdKanan = soundPool.play(soundToPlay, 0f, iidKanan, 1, -1, 1f)
                    }, itdVolume.toLong())
                }
                sudutTheta == 90 -> {
                    // Tengah-tengah: Mainkan suara kiri dan kanan bersamaan
                    streamIdKiri = soundPool.play(soundToPlay, iidKiri, 0f, 1, -1, 1f)
                    streamIdKanan = soundPool.play(soundToPlay, 0f, iidKanan, 1, -1, 1f)
                }
                sudutTheta < 90 -> {
                    // Kuadran 1: Suara kanan dulu, kiri dengan delay ITD
                    streamIdKanan = soundPool.play(soundToPlay, 0f, iidKanan, 1, -1, 1f)
                    Handler(Looper.getMainLooper()).postDelayed({
                        streamIdKiri = soundPool.play(soundToPlay, iidKiri, 0f, 1, -1, 1f)
                    }, itdVolume.toLong())
                }
            }
        } else {
            // Jika suara sudah diputar, perbarui volume tanpa menghentikan suara
            soundPool.setVolume(streamIdKiri, volume, 0f)
            soundPool.setVolume(streamIdKanan, 0f, volume)
        }
    }

    // Fungsi untuk menghentikan suara secara manual
    fun stopBuzzingSound2Channel() {
        if (streamIdKiri != 0) {
            soundPool.stop(streamIdKiri)
            streamIdKiri = 0 // Reset streamIdKiri setelah dihentikan
        }
        if (streamIdKanan != 0) {
            soundPool.stop(streamIdKanan)
            streamIdKanan = 0 // Reset streamIdKanan setelah dihentikan
        }
    }


    fun playBuzzingSoundDownMixing(
        jari_jari: Float,
        jarakMaksimum: Float,
        sudutTheta: Int
    ) {
        var xPosObjek =  jari_jari * cos(sudutTheta * (Math.PI.toFloat() / 180))
        var yPosObjek =  jari_jari * sin(sudutTheta * (Math.PI.toFloat() / 180))

        var xFRSpeaker = jarakMaksimum * (cos(45f * (Math.PI.toFloat() / 180)))
        var yFRSpeaker = jarakMaksimum * (sin(45f * (Math.PI.toFloat() / 180)))

        var xFLSpeaker = jarakMaksimum * (cos((45f + 90f) * (Math.PI.toFloat() / 180)))
        var yFLSpeaker = jarakMaksimum * (sin((45f + 90f) * (Math.PI.toFloat() / 180)))

        var xRRSpeaker = jarakMaksimum * (cos((45f - 90f) * (Math.PI.toFloat() / 180)))
        var yRRSpeaker = jarakMaksimum * (sin((45f - 90f) * (Math.PI.toFloat() / 180)))

        var xRLSpeaker = jarakMaksimum * (cos((45f + 180f) * (Math.PI.toFloat() / 180)))
        var yRLSpeaker = jarakMaksimum * (sin((45f + 180f) * (Math.PI.toFloat() / 180)))

        var xCenterSpeaker = 0f
        var yCenterSpeaker = jarakMaksimum

        var rFRSpeakerObjek     = sqrt((xFRSpeaker - xPosObjek).pow(2) + (yFRSpeaker - yPosObjek).pow(2) ) // Vektor FR
        var rFLSpeakerObjek     = sqrt((xFLSpeaker - xPosObjek).pow(2) + (yFLSpeaker - yPosObjek).pow(2) ) // Vektor FL
        var rRRSpeakerObjek     = sqrt((xRRSpeaker - xPosObjek).pow(2) + (yRRSpeaker - yPosObjek).pow(2) ) // Vektor RR
        var rRLSpeakerObjek     = sqrt((xRLSpeaker - xPosObjek).pow(2) + (yRLSpeaker - yPosObjek).pow(2) ) // Vektor RL
        var rCenterSpeakerObjek = sqrt((xCenterSpeaker - xPosObjek).pow(2) + (yCenterSpeaker - yPosObjek).pow(2) ) //  Vektor Center

        // Menghitung volume untuk setiap speaker berdasarkan jarak
        val volumeFR = (1 - (rFRSpeakerObjek / jarakMaksimum)).coerceIn(0f, 1f) // Volume Front Right
        val volumeFL = (1 - (rFLSpeakerObjek / jarakMaksimum)).coerceIn(0f, 1f) // Volume Front Left
        val volumeRR = (1 - (rRRSpeakerObjek / jarakMaksimum)).coerceIn(0f, 1f) // Volume Rear Right
        val volumeRL = (1 - (rRLSpeakerObjek / jarakMaksimum)).coerceIn(0f, 1f) // Volume Rear Left
        val volumeCenter = (1 - (rCenterSpeakerObjek / jarakMaksimum)).coerceIn(0f, 1f) // Volume Center

        // Hitung volume akhir untuk kanal kiri dan kanan
        val finalVolumeLeft = (volumeFL + volumeRL + volumeCenter) / 3 // Rata-rata volume untuk kiri
        val finalVolumeRight = (volumeFR + volumeRR + volumeCenter) / 3 // Rata-rata volume untuk kanan

    }

    // Fungsi untuk membersihkan resource SoundPool no memory leak mwahahah
    fun release() {
        soundPool.release()
    }
}