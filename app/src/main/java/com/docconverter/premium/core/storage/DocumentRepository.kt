package com.docconverter.premium.core.storage

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.docconverter.premium.core.models.Document
import com.docconverter.premium.core.models.DocumentFormat
import com.docconverter.premium.core.models.DocumentMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for document repository operations
 */
interface DocumentRepository {
    /**
     * Get all documents
     */
    fun getAllDocuments(): Flow<List<Document>>

    /**
     * Get document by ID
     */
    suspend fun getDocumentById(id: String): Document?

    /**
     * Get documents by format
     */
    fun getDocumentsByFormat(format: DocumentFormat): Flow<List<Document>>

    /**
     * Search documents by name or content
     */
    fun searchDocuments(query: String): Flow<List<Document>>

    /**
     * Save a document
     */
    suspend fun saveDocument(document: Document): Document

    /**
     * Delete a document
     */
    suspend fun deleteDocument(document: Document): Boolean

    /**
     * Get recent documents
     */
    fun getRecentDocuments(limit: Int = 10): Flow<List<Document>>
}

/**
 * Implementation of the document repository using Room database
 */
@Singleton
class DocumentRepositoryImpl @Inject constructor(
    private val context: Context,
    private val database: AppDatabase
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> {
        return database.documentDao().getAllDocuments()
            .map { entities -> entities.map { it.toDocument() } }
    }

    override suspend fun getDocumentById(id: String): Document? = withContext(Dispatchers.IO) {
        val entity = database.documentDao().getDocumentById(id)
        entity?.toDocument()
    }

    override fun getDocumentsByFormat(format: DocumentFormat): Flow<List<Document>> {
        return database.documentDao().getDocumentsByFormat(format.id)
            .map { entities -> entities.map { it.toDocument() } }
    }

    override fun searchDocuments(query: String): Flow<List<Document>> {
        val searchQuery = "%$query%"
        return database.documentDao().searchDocuments(searchQuery)
            .map { entities -> entities.map { it.toDocument() } }
    }

    override suspend fun saveDocument(document: Document): Document = withContext(Dispatchers.IO) {
        // Insert or update the document in the database
        val entity = DocumentEntity.fromDocument(document)

        if (database.documentDao().getDocumentById(document.id) != null) {
            database.documentDao().updateDocument(entity)
        } else {
            database.documentDao().insertDocument(entity)
        }

        // Return the saved document
        document
    }

    override suspend fun deleteDocument(document: Document): Boolean = withContext(Dispatchers.IO) {
        try {
            // Delete from database
            database.documentDao().deleteDocument(DocumentEntity.fromDocument(document))

            // Delete the file if it exists locally
            document.filePath?.let { path ->
                val file = File(path)
                if (file.exists()) {
                    file.delete()
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getRecentDocuments(limit: Int): Flow<List<Document>> {
        return database.documentDao().getRecentDocuments(limit)
            .map { entities -> entities.map { it.toDocument() } }
    }
}

/**
 * Room database for document storage
 */
@Database(entities = [DocumentEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao

    companion object {
        private const val DATABASE_NAME = "premium_doc_converter.db"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}

/**
 * Document entity for Room database
 */
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val formatId: String,
    val size: Long,
    val uriString: String,
    val filePath: String?,
    val dateCreated: Date,
    val dateModified: Date,
    val thumbnailUriString: String?,
    val metadataJson: String
) {
    companion object {
        fun fromDocument(document: Document): DocumentEntity {
            return DocumentEntity(
                id = document.id,
                name = document.name,
                formatId = document.format.id,
                size = document.size,
                uriString = document.uri.toString(),
                filePath = document.filePath,
                dateCreated = document.dateCreated,
                dateModified = document.dateModified,
                thumbnailUriString = document.thumbnail?.toString(),
                metadataJson = "{}" // In a real implementation, would serialize metadata to JSON
            )
        }
    }

    fun toDocument(): Document {
        val format = DocumentFormat.getFormatByExtension(name.substringAfterLast('.', ""))
            ?: DocumentFormat.TXT

        return Document(
            id = id,
            name = name,
            format = format,
            size = size,
            uri = Uri.parse(uriString),
            filePath = filePath,
            dateCreated = dateCreated,
            dateModified = dateModified,
            thumbnail = thumbnailUriString?.let { Uri.parse(it) },
            metadata = DocumentMetadata() // In a real implementation, would deserialize from JSON
        )
    }
}

/**
 * Data access object for document entities
 */
@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY dateModified DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE formatId = :formatId ORDER BY dateModified DESC")
    fun getDocumentsByFormat(formatId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE name LIKE :query ORDER BY dateModified DESC")
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY dateModified DESC LIMIT :limit")
    fun getRecentDocuments(limit: Int): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)
}

/**
 * Type converters for Room database
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
