package com.example.valuefy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textOutput: TextView
    private lateinit var recordButton: Button
    private lateinit var stopButton: Button
    private lateinit var playButton: Button
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private val audioFilePath = "${externalCacheDir?.absolutePath}/recorded_audio.3gp"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textOutput = findViewById(R.id.textView)
        recordButton = findViewById(R.id.button2)
        stopButton = findViewById(R.id.button3)
        playButton = findViewById(R.id.button4)

        checkPermission()

        recordButton.setOnClickListener { startRecording() }
        stopButton.setOnClickListener { stopRecording() }
        playButton.setOnClickListener { playRecording() }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    private fun startRecording() {
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)
            try {
                prepare()
                start()
                textOutput.text = "Recording started..."
            } catch (e: IOException) {
                textOutput.text = "Recording failed: ${e.message}"
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        textOutput.text = "Recording stopped."
    }

    private fun playRecording() {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioFilePath)
                prepare()
                start()
                textOutput.text = "Playing recording..."
            } catch (e: IOException) {
                textOutput.text = "Playback failed: ${e.message}"
            }
        }
    }
}
