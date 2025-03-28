package com.docconverter.premium

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class DocConverterApp : Application() {

    companion object {
        const val APP_NAME = "PremiumDocConverter"
        const val APP_VERSION = "1.0.0"

        lateinit var instance: DocConverterApp
            private set
    }

    // Application directories
    lateinit var cacheDir: File
    lateinit var conversionDir: File
    lateinit var outputDir: File
    lateinit var tempDir: File

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize directories
        initializeDirectories()

        // Initialize crash reporting (would be implemented in production)
        setupCrashReporting()
    }

    private fun initializeDirectories() {
        // Set up app directories
        cacheDir = File(filesDir, "cache").apply { mkdirs() }
        conversionDir = File(filesDir, "conversions").apply { mkdirs() }
        outputDir = File(getExternalFilesDir(null), "outputs").apply { mkdirs() }
        tempDir = File(cacheDir, "temp").apply { mkdirs() }

        // Clean temporary files on startup
        cleanTempFiles()
    }

    private fun cleanTempFiles() {
        tempDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.delete()
            }
        }
    }

    private fun setupCrashReporting() {
        // In a production app, would initialize crash reporting service here
    }

    /**
     * Creates a new temporary file for conversion processing
     */
    fun createTempFile(prefix: String, suffix: String): File {
        return File.createTempFile(prefix, suffix, tempDir)
    }

    /**
     * Creates an output file with a unique name
     */
    fun createOutputFile(fileName: String, format: String): File {
        val timestamp = System.currentTimeMillis()
        val name = fileName.substringBeforeLast(".")
        return File(outputDir, "${name}_$timestamp.$format")
    }
}
