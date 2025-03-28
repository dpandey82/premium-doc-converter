package com.docconverter.premium.core.models

/**
 * Represents a document format category and its associated file extensions
 */
enum class FormatCategory {
    DOCUMENT,
    SPREADSHEET,
    PRESENTATION,
    EMAIL,
    IMAGE,
    MARKUP,
    EBOOK,
    ARCHIVE,
    PLAIN_TEXT
}

/**
 * Represents a document format with its properties
 */
data class DocumentFormat(
    val id: String,
    val name: String,
    val extension: String,
    val mimeType: String,
    val category: FormatCategory,
    val isInputSupported: Boolean = true,
    val isOutputSupported: Boolean = true,
    val requiresOcr: Boolean = false
) {
    companion object {
        // Document formats
        val PDF = DocumentFormat("pdf", "PDF", "pdf", "application/pdf", FormatCategory.DOCUMENT)
        val DOCX = DocumentFormat("docx", "Word Document", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", FormatCategory.DOCUMENT)
        val DOC = DocumentFormat("doc", "Word Document (Legacy)", "doc", "application/msword", FormatCategory.DOCUMENT)
        val RTF = DocumentFormat("rtf", "Rich Text Format", "rtf", "application/rtf", FormatCategory.DOCUMENT)
        val ODT = DocumentFormat("odt", "OpenDocument Text", "odt", "application/vnd.oasis.opendocument.text", FormatCategory.DOCUMENT)
        val PAGES = DocumentFormat("pages", "Apple Pages", "pages", "application/x-iwork-pages-sffpages", FormatCategory.DOCUMENT, isOutputSupported = false)

        // Spreadsheet formats
        val XLSX = DocumentFormat("xlsx", "Excel Workbook", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", FormatCategory.SPREADSHEET)
        val XLS = DocumentFormat("xls", "Excel Workbook (Legacy)", "xls", "application/vnd.ms-excel", FormatCategory.SPREADSHEET)
        val ODS = DocumentFormat("ods", "OpenDocument Spreadsheet", "ods", "application/vnd.oasis.opendocument.spreadsheet", FormatCategory.SPREADSHEET)
        val CSV = DocumentFormat("csv", "Comma Separated Values", "csv", "text/csv", FormatCategory.SPREADSHEET)
        val TSV = DocumentFormat("tsv", "Tab Separated Values", "tsv", "text/tab-separated-values", FormatCategory.SPREADSHEET)

        // Presentation formats
        val PPTX = DocumentFormat("pptx", "PowerPoint Presentation", "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", FormatCategory.PRESENTATION)
        val PPT = DocumentFormat("ppt", "PowerPoint Presentation (Legacy)", "ppt", "application/vnd.ms-powerpoint", FormatCategory.PRESENTATION)
        val ODP = DocumentFormat("odp", "OpenDocument Presentation", "odp", "application/vnd.oasis.opendocument.presentation", FormatCategory.PRESENTATION)
        val KEY = DocumentFormat("key", "Apple Keynote", "key", "application/x-iwork-keynote-sffkey", FormatCategory.PRESENTATION, isOutputSupported = false)

        // Email formats
        val MSG = DocumentFormat("msg", "Outlook Message", "msg", "application/vnd.ms-outlook", FormatCategory.EMAIL)
        val EML = DocumentFormat("eml", "Email Message", "eml", "message/rfc822", FormatCategory.EMAIL)
        val MBOX = DocumentFormat("mbox", "Mail Box", "mbox", "application/mbox", FormatCategory.EMAIL)

        // Image formats
        val JPG = DocumentFormat("jpg", "JPEG Image", "jpg", "image/jpeg", FormatCategory.IMAGE, requiresOcr = true)
        val PNG = DocumentFormat("png", "PNG Image", "png", "image/png", FormatCategory.IMAGE, requiresOcr = true)
        val TIFF = DocumentFormat("tiff", "TIFF Image", "tiff", "image/tiff", FormatCategory.IMAGE, requiresOcr = true)
        val BMP = DocumentFormat("bmp", "Bitmap Image", "bmp", "image/bmp", FormatCategory.IMAGE, requiresOcr = true)
        val WEBP = DocumentFormat("webp", "WebP Image", "webp", "image/webp", FormatCategory.IMAGE, requiresOcr = true)
        val GIF = DocumentFormat("gif", "GIF Image", "gif", "image/gif", FormatCategory.IMAGE, requiresOcr = true)

        // Markup formats
        val MD = DocumentFormat("md", "Markdown", "md", "text/markdown", FormatCategory.MARKUP)
        val HTML = DocumentFormat("html", "HTML", "html", "text/html", FormatCategory.MARKUP)
        val XML = DocumentFormat("xml", "XML", "xml", "text/xml", FormatCategory.MARKUP)
        val JSON = DocumentFormat("json", "JSON", "json", "application/json", FormatCategory.MARKUP)
        val YAML = DocumentFormat("yaml", "YAML", "yaml", "application/x-yaml", FormatCategory.MARKUP)
        val LATEX = DocumentFormat("latex", "LaTeX", "tex", "application/x-latex", FormatCategory.MARKUP)

        // eBook formats
        val EPUB = DocumentFormat("epub", "EPUB eBook", "epub", "application/epub+zip", FormatCategory.EBOOK)
        val MOBI = DocumentFormat("mobi", "Kindle MOBI", "mobi", "application/x-mobipocket-ebook", FormatCategory.EBOOK)
        val AZW = DocumentFormat("azw", "Kindle AZW", "azw", "application/vnd.amazon.ebook", FormatCategory.EBOOK)
        val AZW3 = DocumentFormat("azw3", "Kindle AZW3", "azw3", "application/vnd.amazon.ebook", FormatCategory.EBOOK)

        // Archive formats
        val ZIP = DocumentFormat("zip", "ZIP Archive", "zip", "application/zip", FormatCategory.ARCHIVE)
        val RAR = DocumentFormat("rar", "RAR Archive", "rar", "application/x-rar-compressed", FormatCategory.ARCHIVE)
        val SEVEN_Z = DocumentFormat("7z", "7-Zip Archive", "7z", "application/x-7z-compressed", FormatCategory.ARCHIVE)

        // Plain text
        val TXT = DocumentFormat("txt", "Plain Text", "txt", "text/plain", FormatCategory.PLAIN_TEXT)

        // All supported formats
        val ALL_FORMATS = listOf(
            PDF, DOCX, DOC, RTF, ODT, PAGES,
            XLSX, XLS, ODS, CSV, TSV,
            PPTX, PPT, ODP, KEY,
            MSG, EML, MBOX,
            JPG, PNG, TIFF, BMP, WEBP, GIF,
            MD, HTML, XML, JSON, YAML, LATEX,
            EPUB, MOBI, AZW, AZW3,
            ZIP, RAR, SEVEN_Z,
            TXT
        )

        // Get formats by category
        fun getFormatsByCategory(category: FormatCategory): List<DocumentFormat> {
            return ALL_FORMATS.filter { it.category == category }
        }

        // Get input formats
        fun getInputFormats(): List<DocumentFormat> {
            return ALL_FORMATS.filter { it.isInputSupported }
        }

        // Get output formats
        fun getOutputFormats(): List<DocumentFormat> {
            return ALL_FORMATS.filter { it.isOutputSupported }
        }

        // Get format by extension
        fun getFormatByExtension(extension: String): DocumentFormat? {
            return ALL_FORMATS.find { it.extension.equals(extension, ignoreCase = true) }
        }

        // Get format by mime type
        fun getFormatByMimeType(mimeType: String): DocumentFormat? {
            return ALL_FORMATS.find { it.mimeType.equals(mimeType, ignoreCase = true) }
        }
    }
}
