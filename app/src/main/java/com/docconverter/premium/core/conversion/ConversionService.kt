package com.docconverter.premium.core.conversion

import com.docconverter.premium.core.models.ConversionResult
import com.docconverter.premium.core.models.Document
import com.docconverter.premium.core.models.DocumentFormat
import com.docconverter.premium.core.models.VerificationResult
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining document conversion operations
 */
interface ConversionService {
    /**
     * Convert a document to a specified format
     * @param document Source document to convert
     * @param targetFormat Format to convert to
     * @param options Conversion options
     * @return Flow of ConversionProgress including the final result
     */
    suspend fun convertDocument(
        document: Document,
        targetFormat: DocumentFormat,
        options: ConversionOptions = ConversionOptions()
    ): Flow<ConversionProgress>

    /**
     * Batch convert multiple documents
     * @param documents List of documents to convert
     * @param targetFormat Format to convert to
     * @param options Conversion options
     * @return Flow of BatchConversionProgress
     */
    suspend fun batchConvertDocuments(
        documents: List<Document>,
        targetFormat: DocumentFormat,
        options: ConversionOptions = ConversionOptions()
    ): Flow<BatchConversionProgress>

    /**
     * Verify the conversion result against the original
     * @param sourceDocument Original document
     * @param convertedDocument Converted document
     * @param options Verification options
     * @return Flow of VerificationProgress including the final result
     */
    suspend fun verifyConversion(
        sourceDocument: Document,
        convertedDocument: Document,
        options: VerificationOptions = VerificationOptions()
    ): Flow<VerificationProgress>

    /**
     * Extract content from a document without conversion
     * @param document Document to extract from
     * @param options Extraction options
     * @return Extracted document content
     */
    suspend fun extractContent(
        document: Document,
        options: ExtractionOptions = ExtractionOptions()
    ): DocumentContent

    /**
     * Extract metadata from a document
     * @param document Document to extract metadata from
     * @return Document with updated metadata
     */
    suspend fun extractMetadata(document: Document): Document

    /**
     * Check if conversion between specified formats is supported
     * @param sourceFormat Source document format
     * @param targetFormat Target document format
     * @return true if conversion is supported
     */
    fun isConversionSupported(sourceFormat: DocumentFormat, targetFormat: DocumentFormat): Boolean

    /**
     * Get all supported conversion paths for a given format
     * @param sourceFormat Source document format
     * @return List of supported target formats
     */
    fun getSupportedTargetFormats(sourceFormat: DocumentFormat): List<DocumentFormat>
}

/**
 * Options for document conversion
 */
data class ConversionOptions(
    val preserveFormatting: Boolean = true,
    val preserveImages: Boolean = true,
    val preserveFonts: Boolean = true,
    val preserveMetadata: Boolean = true,
    val preserveHyperlinks: Boolean = true,
    val preserveHeadersFooters: Boolean = true,
    val preservePageNumbers: Boolean = true,
    val autoVerify: Boolean = true,
    val compressionLevel: CompressionLevel = CompressionLevel.MEDIUM,
    val password: String? = null, // For password-protected documents
    val customOptions: Map<String, Any> = emptyMap() // Format-specific options
)

/**
 * Options for conversion verification
 */
data class VerificationOptions(
    val verifyContent: Boolean = true,
    val verifyFormatting: Boolean = true,
    val verifyStructure: Boolean = true,
    val verifyMetadata: Boolean = true,
    val minimumMatchScore: Float = 0.9f, // Minimum match score (0.0-1.0) to consider successful
    val generateReport: Boolean = true
)

/**
 * Options for content extraction
 */
data class ExtractionOptions(
    val extractText: Boolean = true,
    val extractImages: Boolean = true,
    val extractTables: Boolean = true,
    val preserveFormatting: Boolean = true,
    val extractHyperlinks: Boolean = true
)

/**
 * Compression level for output documents
 */
enum class CompressionLevel {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Represents document content extracted from a document
 */
data class DocumentContent(
    val text: String,
    val formattedText: String? = null, // HTML or RTF representation
    val images: List<DocumentImage> = emptyList(),
    val tables: List<DocumentTable> = emptyList(),
    val links: List<DocumentLink> = emptyList(),
    val structure: DocumentStructure? = null
)

/**
 * Represents an image within a document
 */
data class DocumentImage(
    val id: String,
    val data: ByteArray,
    val mimeType: String,
    val width: Int,
    val height: Int,
    val description: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentImage

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Represents a table within a document
 */
data class DocumentTable(
    val id: String,
    val rows: Int,
    val columns: Int,
    val cells: List<List<String>>
)

/**
 * Represents a hyperlink within a document
 */
data class DocumentLink(
    val text: String,
    val url: String
)

/**
 * Represents the document structure
 */
data class DocumentStructure(
    val headings: List<Heading> = emptyList(),
    val paragraphs: Int = 0,
    val sections: Int = 0,
    val pages: Int = 0
)

/**
 * Represents a heading in document structure
 */
data class Heading(
    val text: String,
    val level: Int,
    val pageNumber: Int? = null
)

/**
 * Represents the progress of a conversion operation
 */
sealed class ConversionProgress {
    data class Initializing(val document: Document) : ConversionProgress()
    data class Processing(val document: Document, val progress: Float) : ConversionProgress()
    data class Verifying(val document: Document, val convertedDocument: Document) : ConversionProgress()
    data class Completed(val result: ConversionResult) : ConversionProgress()
    data class Failed(val document: Document, val error: String) : ConversionProgress()
}

/**
 * Represents the progress of a batch conversion operation
 */
data class BatchConversionProgress(
    val totalDocuments: Int,
    val processedDocuments: Int,
    val currentDocumentProgress: Float,
    val currentDocument: Document?,
    val results: List<ConversionResult> = emptyList(),
    val failed: List<Pair<Document, String>> = emptyList()
)

/**
 * Represents the progress of a verification operation
 */
sealed class VerificationProgress {
    data class Initializing(val sourceDocument: Document, val convertedDocument: Document) : VerificationProgress()
    data class Comparing(val sourceDocument: Document, val convertedDocument: Document, val progress: Float) : VerificationProgress()
    data class Completed(val result: VerificationResult) : VerificationProgress()
    data class Failed(val error: String) : VerificationProgress()
}
