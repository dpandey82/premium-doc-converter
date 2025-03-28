package com.docconverter.premium.core.models

import android.net.Uri
import java.io.File
import java.util.Date

/**
 * Represents a document in the system with its metadata
 */
data class Document(
    val id: String,
    val name: String,
    val format: DocumentFormat,
    val size: Long,
    val uri: Uri,
    val filePath: String? = null, // May be null for content Uri based documents
    val dateCreated: Date = Date(),
    val dateModified: Date = Date(),
    val thumbnail: Uri? = null,
    val metadata: DocumentMetadata = DocumentMetadata()
) {
    /**
     * Check if the file exists locally
     */
    fun exists(): Boolean {
        return filePath?.let { File(it).exists() } ?: false
    }

    /**
     * Get the file extension
     */
    fun getExtension(): String {
        return format.extension
    }

    /**
     * Get a formatted file size string
     */
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
}

/**
 * Represents document metadata extracted during import or conversion
 */
data class DocumentMetadata(
    val title: String? = null,
    val author: String? = null,
    val subject: String? = null,
    val keywords: List<String> = emptyList(),
    val creator: String? = null,
    val producer: String? = null,
    val creationDate: Date? = null,
    val modificationDate: Date? = null,
    val pageCount: Int = 0,
    val isEncrypted: Boolean = false,
    val isPasswordProtected: Boolean = false,
    val properties: Map<String, String> = emptyMap()
)

/**
 * Represents the result of a conversion operation
 */
data class ConversionResult(
    val sourceDocument: Document,
    val outputDocument: Document,
    val success: Boolean,
    val verificationResult: VerificationResult? = null,
    val conversionTime: Long = 0, // in milliseconds
    val errorMessage: String? = null
)

/**
 * Represents the result of format verification
 */
data class VerificationResult(
    val success: Boolean,
    val score: Float, // 0.0 to 1.0 representing match percentage
    val contentMatchScore: Float, // Content match score
    val formattingMatchScore: Float, // Formatting match score
    val structureMatchScore: Float, // Structure match score
    val metadataMatchScore: Float, // Metadata match score
    val issues: List<VerificationIssue> = emptyList()
)

/**
 * Represents a verification issue found during the verification process
 */
data class VerificationIssue(
    val type: IssueType,
    val description: String,
    val severity: IssueSeverity,
    val location: String? = null // Page number, paragraph, element ID, etc.
)

/**
 * Type of verification issue
 */
enum class IssueType {
    CONTENT_MISMATCH,
    FORMATTING_MISMATCH,
    STRUCTURE_MISMATCH,
    METADATA_MISMATCH,
    RESOURCE_MISSING,
    FONT_SUBSTITUTION
}

/**
 * Severity level of verification issue
 */
enum class IssueSeverity {
    CRITICAL, // Breaks document integrity
    HIGH,     // Significant format issues
    MEDIUM,   // Noticeable format issues
    LOW,      // Minor format issues
    INFO      // Information only
}
