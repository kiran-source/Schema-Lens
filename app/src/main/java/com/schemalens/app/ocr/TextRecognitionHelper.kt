package com.schemalens.app.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.schemalens.app.data.OcrExtractionResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * On-device ML Kit Text Recognition Helper.
 * Runs 100% on-device (NPU / CPU) with ZERO network calls (Red Light Zone).
 */
object TextRecognitionHelper {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    private val IDENTIFIER_REGEX = Regex("""\b[A-Za-z_][A-Za-z0-9_]{2,}\b""")

    /**
     * Extracts text and candidate schema identifiers from a Bitmap on-device.
     */
    suspend fun processImage(bitmap: Bitmap): OcrExtractionResult = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                val identifiers = extractIdentifiers(fullText)
                continuation.resume(OcrExtractionResult(fullText, identifiers))
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }

    /**
     * Extracts text and candidate schema identifiers from a content Uri on-device.
     */
    suspend fun processImageUri(context: Context, uri: Uri): OcrExtractionResult = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, uri)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val fullText = visionText.text
                    val identifiers = extractIdentifiers(fullText)
                    continuation.resume(OcrExtractionResult(fullText, identifiers))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    /**
     * Extracts candidate SQL/ORM identifiers matching `[A-Za-z_][A-Za-z0-9_]{2,}`, deduplicated.
     */
    fun extractIdentifiers(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return IDENTIFIER_REGEX.findAll(text)
            .map { it.value }
            .distinct()
            .toList()
    }
}
