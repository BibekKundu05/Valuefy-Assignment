package com.example.valuefy_demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.RecognitionListener
import java.io.*

class MainActivity : AppCompatActivity(), RecognitionListener {

    private lateinit var tvTranscribedText: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private var speechService: SpeechService? = null
    private var model: Model? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTranscribedText = findViewById(R.id.tvTranscribedText)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)

        // Request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        // Copy model from assets to internal storage
        val modelPath = File(filesDir, "vosk-model")
        if (!modelPath.exists()) {
            copyAssets("vosk-model", modelPath)
        }

        // Load the model
        try {
            model = Model(modelPath.absolutePath)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Vosk", "Model loading failed: ${e.message}")
        }

        btnStart.setOnClickListener {
            startListening()
        }

        btnStop.setOnClickListener {
            stopListening()
        }
    }

    private fun copyAssets(assetPath: String, outputDir: File) {
        try {
            val assetManager = assets
            val files = assetManager.list(assetPath) ?: return

            outputDir.mkdirs()

            for (file in files) {
                val inputStream = assetManager.open("$assetPath/$file")
                val outputFile = File(outputDir, file)

                copyFile(inputStream, outputFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Vosk", "Asset copying failed: ${e.message}")
        }
    }

    private fun copyFile(inputStream: InputStream, outputFile: File) {
        try {
            val outputStream = FileOutputStream(outputFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Vosk", "File copying error: ${e.message}")
        }
    }

    private fun startListening() {
        if (model == null) {
            tvTranscribedText.text = "Error: Model not loaded"
            return
        }

        val recognizer = Recognizer(model, 16000.0f)
        speechService = SpeechService(recognizer, 16000.0f)
        speechService?.startListening(this)

        tvTranscribedText.text = "Listening..."
    }

    private fun stopListening() {
        speechService?.stop()
        speechService = null
        tvTranscribedText.text = "Stopped listening"
    }

    override fun onResult(hypothesis: String?) {
        runOnUiThread {
            tvTranscribedText.text = "Transcription: $hypothesis"
        }
    }

    override fun onPartialResult(hypothesis: String?) {}

    override fun onFinalResult(hypothesis: String?) {
        runOnUiThread {
            tvTranscribedText.text = "Final Transcription: $hypothesis"
        }
    }

    override fun onError(e: Exception?) {
        runOnUiThread {
            tvTranscribedText.text = "Error: ${e?.message}"
        }
    }

    override fun onTimeout() {}

}
