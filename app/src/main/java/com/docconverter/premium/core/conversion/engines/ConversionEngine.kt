package com.docconverter.premium.core.conversion.engines

import com.docconverter.premium.core.conversion.DocumentContent
import com.docconverter.premium.core.conversion.ExtractionOptions
import com.docconverter.premium.core.models.DocumentFormat
import com.docconverter.premium.core.models.DocumentMetadata
import com.docconverter.premium.core.conversion.ConversionOptions
import java.io.File

/**
 * Interface defining operations for a document conversion engine
 */
interface ConversionEngine {
    /**
     * Convert a document from one format to another
     * @param inputFile Source document file
     * @param outputFile Target file to write to
     * @param sourceFormat Source document format
     * @param targetFormat Target document format
     * @param options Conversion options
     * @return true if conversion was successful
     */
    fun convert(
        inputFile: File,
        outputFile: File,
        sourceFormat: DocumentFormat,
        targetFormat: DocumentFormat,
        options: ConversionOptions
    ): Boolean

    /**
     * Extract content from a document without converting
     * @param inputFile Source document file
     * @param format Document format
     * @param options Extraction options
     * @return Extracted document content
     */
    fun extractContent(
        inputFile: File,
        format: DocumentFormat,
        options: ExtractionOptions
    ): DocumentContent

    /**
     * Extract metadata from a document
     * @param inputFile Source document file
     * @param format Document format
     * @return Document metadata
     */
    fun extractMetadata(
        inputFile: File,
        format: DocumentFormat
    ): DocumentMetadata

    /**
     * Check if this engine supports conversion between the specified formats
     * @param sourceFormat Source document format
     * @param targetFormat Target document format
     * @return true if conversion is supported
     */
    fun supportsConversion(sourceFormat: DocumentFormat, targetFormat: DocumentFormat): Boolean
}
