package com.docconverter.premium.core.conversion

import android.content.Context
import android.net.Uri
import com.docconverter.premium.DocConverterApp
import com.docconverter.premium.core.conversion.engines.*
import com.docconverter.premium.core.models.*
import com.docconverter.premium.core.storage.DocumentRepository
import com.docconverter.premium.core.verification.VerificationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the ConversionService interface
 */
@Singleton
class ConversionServiceImpl @Inject constructor(
    private val context: Context,
    private val documentRepository: DocumentRepository,
    private val verificationEngine: VerificationEngine
) : ConversionService {

    // Registry of conversion engines
    private val conversionEngines = mapOf(
        // Document engines
        FormatCategory.DOCUMENT to DocumentConversionEngine(),
        FormatCategory.SPREADSHEET to SpreadsheetConversionEngine(),
        FormatCategory.PRESENTATION to PresentationConversionEngine(),
        FormatCategory.EMAIL to EmailConversionEngine(),
        FormatCategory.IMAGE to ImageConversionEngine(),
        FormatCategory.MARKUP to MarkupConversionEngine(),
        FormatCategory.EBOOK to EbookConversionEngine(),
        FormatCategory.ARCHIVE to ArchiveConversionEngine(),
        FormatCategory.PLAIN_TEXT to PlainTextConversionEngine()
    )

    // Conversion path registry
    private val conversionPaths = buildConversionPaths()

    override suspend fun convertDocument(
        document: Document,
        targetFormat: DocumentFormat,
        options: ConversionOptions
    ): Flow<ConversionProgress> = flow {
        // Emit initializing state
        emit(ConversionProgress.Initializing(document))

        try {
            // Validate conversion is supported
            if (!isConversionSupported(document.format, targetFormat)) {
                emit(ConversionProgress.Failed(document, "Conversion from ${document.format.name} to ${targetFormat.name} is not supported"))
                return@flow
            }

            // Get appropriate conversion engine
            val engine = getConversionEngine(document.format.category)

            // Create temporary files
            val inputFile = createTempInputFile(document)
            val outputFile = DocConverterApp.instance.createTempFile(
                "convert_output",
                ".${targetFormat.extension}"
            )

            // Process conversion
            for (progress in 0..100) {
                val progressPercent = progress / 100f
                emit(ConversionProgress.Processing(document, progressPercent))

                // Simulate conversion steps (in a real implementation, this would be actual conversion logic)
                delay(50) // Simulate processing time
            }

            // Perform actual conversion
            val result = engine.convert(inputFile, outputFile, document.format, targetFormat, options)

            if (!result) {
                emit(ConversionProgress.Failed(document, "Conversion failed during processing"))
                return@flow
            }

            // Create output document
            val outputDocument = createOutputDocument(document, outputFile, targetFormat)

            // Save output document to repository
            val savedDocument = documentRepository.saveDocument(outputDocument)

            // Verify conversion if requested
            var verificationResult: VerificationResult? = null
            if (options.autoVerify) {
                emit(ConversionProgress.Verifying(document, savedDocument))
                verificationResult = verifyConversion(document, savedDocument)
            }

            // Create conversion result
            val conversionResult = ConversionResult(
                sourceDocument = document,
                outputDocument = savedDocument,
                success = true,
                verificationResult = verificationResult,
                conversionTime = 2000 // Placeholder for actual conversion time
            )

            // Emit completion
            emit(ConversionProgress.Completed(conversionResult))

        } catch (e: Exception) {
            emit(ConversionProgress.Failed(document, "Conversion failed: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun batchConvertDocuments(
        documents: List<Document>,
        targetFormat: DocumentFormat,
        options: ConversionOptions
    ): Flow<BatchConversionProgress> = flow {
        val results = mutableListOf<ConversionResult>()
        val failures = mutableListOf<Pair<Document, String>>()

        for ((index, document) in documents.withIndex()) {
            // Emit progress update
            emit(BatchConversionProgress(
                totalDocuments = documents.size,
                processedDocuments = index,
                currentDocumentProgress = 0f,
                currentDocument = document,
                results = results.toList(),
                failed = failures.toList()
            ))

            try {
                // Process each document
                convertDocument(document, targetFormat, options).collect { progress ->
                    when (progress) {
                        is ConversionProgress.Processing -> {
                            emit(BatchConversionProgress(
                                totalDocuments = documents.size,
                                processedDocuments = index,
                                currentDocumentProgress = progress.progress,
                                currentDocument = document,
                                results = results.toList(),
                                failed = failures.toList()
                            ))
                        }
                        is ConversionProgress.Completed -> {
                            results.add(progress.result)
                        }
                        is ConversionProgress.Failed -> {
                            failures.add(Pair(document, progress.error))
                        }
                        else -> { /* Ignore other states */ }
                    }
                }
            } catch (e: Exception) {
                failures.add(Pair(document, "Error: ${e.message}"))
            }
        }

        // Final progress update
        emit(BatchConversionProgress(
            totalDocuments = documents.size,
            processedDocuments = documents.size,
            currentDocumentProgress = 1f,
            currentDocument = null,
            results = results.toList(),
            failed = failures.toList()
        ))
    }.flowOn(Dispatchers.IO)

    override suspend fun verifyConversion(
        sourceDocument: Document,
        convertedDocument: Document,
        options: VerificationOptions
    ): Flow<VerificationProgress> = flow {
        emit(VerificationProgress.Initializing(sourceDocument, convertedDocument))

        try {
            // Simulate verification process
            for (progress in 0..100) {
                val progressPercent = progress / 100f
                emit(VerificationProgress.Comparing(sourceDocument, convertedDocument, progressPercent))
                delay(20) // Simulate verification time
            }

            // Perform actual verification using verification engine
            val result = verificationEngine.verify(sourceDocument, convertedDocument, options)

            emit(VerificationProgress.Completed(result))
        } catch (e: Exception) {
            emit(VerificationProgress.Failed("Verification failed: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun extractContent(
        document: Document,
        options: ExtractionOptions
    ): DocumentContent = withContext(Dispatchers.IO) {
        // Get appropriate conversion engine
        val engine = getConversionEngine(document.format.category)

        // Create temporary file
        val inputFile = createTempInputFile(document)

        // Extract content
        engine.extractContent(inputFile, document.format, options)
    }

    override suspend fun extractMetadata(document: Document): Document = withContext(Dispatchers.IO) {
        // Get appropriate conversion engine
        val engine = getConversionEngine(document.format.category)

        // Create temporary file
        val inputFile = createTempInputFile(document)

        // Extract metadata
        val metadata = engine.extractMetadata(inputFile, document.format)

        // Create updated document with metadata
        document.copy(metadata = metadata)
    }

    override fun isConversionSupported(sourceFormat: DocumentFormat, targetFormat: DocumentFormat): Boolean {
        return conversionPaths[sourceFormat.id]?.contains(targetFormat.id) ?: false
    }

    override fun getSupportedTargetFormats(sourceFormat: DocumentFormat): List<DocumentFormat> {
        val targetFormatIds = conversionPaths[sourceFormat.id] ?: emptySet()
        return DocumentFormat.ALL_FORMATS.filter { it.id in targetFormatIds }
    }

    /**
     * Get the conversion engine for a specific format category
     */
    private fun getConversionEngine(category: FormatCategory): ConversionEngine {
        return conversionEngines[category] ?: throw IllegalArgumentException("No conversion engine for category: $category")
    }

    /**
     * Create a temporary file from a document
     */
    private suspend fun createTempInputFile(document: Document): File {
        val tempFile = DocConverterApp.instance.createTempFile(
            "convert_input",
            ".${document.format.extension}"
        )

        // Copy document content to temp file
        document.uri.let { uri ->
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return tempFile
    }

    /**
     * Create an output document from a converted file
     */
    private fun createOutputDocument(sourceDocument: Document, outputFile: File, targetFormat: DocumentFormat): Document {
        val docId = UUID.randomUUID().toString()

        // Create final output file in app storage
        val finalOutputFile = DocConverterApp.instance.createOutputFile(
            sourceDocument.name,
            targetFormat.extension
        )

        // Copy temporary file to final location
        outputFile.copyTo(finalOutputFile, overwrite = true)

        // Create output document
        return Document(
            id = docId,
            name = "${sourceDocument.name.substringBeforeLast(".")}.${targetFormat.extension}",
            format = targetFormat,
            size = finalOutputFile.length(),
            uri = Uri.fromFile(finalOutputFile),
            filePath = finalOutputFile.absolutePath,
            dateCreated = Date(),
            dateModified = Date(),
            metadata = sourceDocument.metadata.copy() // Copy metadata from source
        )
    }

    /**
     * Helper function to verify conversion internally
     */
    private suspend fun verifyConversion(
        sourceDocument: Document,
        convertedDocument: Document
    ): VerificationResult {
        return verificationEngine.verify(
            sourceDocument,
            convertedDocument,
            VerificationOptions()
        )
    }

    /**
     * Build the conversion paths map
     */
    private fun buildConversionPaths(): Map<String, Set<String>> {
        val paths = mutableMapOf<String, MutableSet<String>>()

        // Document formats conversion paths
        val documentFormats = setOf(
            DocumentFormat.PDF.id, DocumentFormat.DOCX.id, DocumentFormat.DOC.id,
            DocumentFormat.RTF.id, DocumentFormat.ODT.id, DocumentFormat.PAGES.id
        )

        for (sourceFormat in documentFormats) {
            val targetFormats = mutableSetOf<String>()
            targetFormats.addAll(documentFormats)
            targetFormats.add(DocumentFormat.TXT.id)
            targetFormats.add(DocumentFormat.HTML.id)
            targetFormats.add(DocumentFormat.MD.id)
            paths[sourceFormat] = targetFormats
        }

        // Spreadsheet formats conversion paths
        val spreadsheetFormats = setOf(
            DocumentFormat.XLSX.id, DocumentFormat.XLS.id, DocumentFormat.ODS.id,
            DocumentFormat.CSV.id, DocumentFormat.TSV.id
        )

        for (sourceFormat in spreadsheetFormats) {
            val targetFormats = mutableSetOf<String>()
            targetFormats.addAll(spreadsheetFormats)
            targetFormats.add(DocumentFormat.PDF.id)
            targetFormats.add(DocumentFormat.HTML.id)
            targetFormats.add(DocumentFormat.TXT.id)
            paths[sourceFormat] = targetFormats
        }

        // Presentation formats conversion paths
        val presentationFormats = setOf(
            DocumentFormat.PPTX.id, DocumentFormat.PPT.id, DocumentFormat.ODP.id,
            DocumentFormat.KEY.id
        )

        for (sourceFormat in presentationFormats) {
            val targetFormats = mutableSetOf<String>()
            targetFormats.addAll(presentationFormats)
            targetFormats.add(DocumentFormat.PDF.id)
            paths[sourceFormat] = targetFormats
        }

        // Email formats conversion paths
        val emailFormats = setOf(
            DocumentFormat.MSG.id, DocumentFormat.EML.id, DocumentFormat.MBOX.id
        )

        for (sourceFormat in emailFormats) {
            val targetFormats = mutableSetOf<String>()
            targetFormats.addAll(emailFormats)
            targetFormats.add(DocumentFormat.PDF.id)
            targetFormats.add(DocumentFormat.TXT.id)
            targetFormats.add(DocumentFormat.HTML.id)
            paths[sourceFormat] = targetFormats
        }

        // Image formats conversion paths
        val imageFormats = setOf(
            DocumentFormat.JPG.id, DocumentFormat.PNG.id, DocumentFormat.TIFF.id,
            DocumentFormat.BMP.id, DocumentFormat.WEBP.id, DocumentFormat.GIF.id
        )

        for (sourceFormat in imageFormats) {
            val targetFormats = mutableSetOf<String>()
            targetFormats.addAll(imageFormats)
            targetFormats.add(DocumentFormat.PDF.id)
            targetFormats.add(DocumentFormat.TXT.id) // Via OCR
            targetFormats.add(DocumentFormat.DOCX.id) // Via OCR
            paths[sourceFormat] = targetFormats
        }

        // Markup formats conversion paths
        val markupFormats = setOf(
            DocumentFormat.MD.id, DocumentFormat.HTML.id, DocumentFormat.XML.id,
            DocumentFormat.JSON.id, DocumentFormat.YAML.id, DocumentFormat.LATEX.id
        )

        for (sourceFormat in markupFormats) {
            val targetFormats = mutableSetOf<String>()
            targetFormats.addAll(markupFormats)
            targetFormats.add(DocumentFormat.PDF.id)
            targetFormats.add(DocumentFormat.DOCX.id)
            targetFormats.add(DocumentFormat.TXT.id)
            paths[sourceFormat] = targetFormats
        }

        // eBook formats conversion paths
        val ebookFormats = setOf(
            DocumentFormat.EPUB.id, DocumentFormat.MOBI.id, DocumentFormat.AZW.id,
            DocumentFormat.AZW3.id
        )

        for (sourceFormat in ebookFormats) {
            val targetFormats = mutableSetOf<String>()
            targetFormats.addAll(ebookFormats)
            targetFormats.add(DocumentFormat.PDF.id)
            targetFormats.add(DocumentFormat.DOCX.id)
            targetFormats.add(DocumentFormat.TXT.id)
            targetFormats.add(DocumentFormat.HTML.id)
            paths[sourceFormat] = targetFormats
        }

        // Archive formats conversion paths
        val archiveFormats = setOf(
            DocumentFormat.ZIP.id, DocumentFormat.RAR.id, DocumentFormat.SEVEN_Z.id
        )

        for (sourceFormat in archiveFormats) {
            val targetFormats = mutableSetOf<String>()
            // Archives can only be extracted, not converted
            paths[sourceFormat] = targetFormats
        }

        // Plain text conversion paths
        paths[DocumentFormat.TXT.id] = mutableSetOf(
            DocumentFormat.TXT.id,
            DocumentFormat.PDF.id,
            DocumentFormat.DOCX.id,
            DocumentFormat.HTML.id,
            DocumentFormat.MD.id,
            DocumentFormat.RTF.id
        )

        return paths
    }
}
