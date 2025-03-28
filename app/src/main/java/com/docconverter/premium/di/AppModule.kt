package com.docconverter.premium.di

import android.content.Context
import com.docconverter.premium.core.conversion.ConversionService
import com.docconverter.premium.core.conversion.ConversionServiceImpl
import com.docconverter.premium.core.storage.AppDatabase
import com.docconverter.premium.core.storage.DocumentRepository
import com.docconverter.premium.core.storage.DocumentRepositoryImpl
import com.docconverter.premium.core.verification.VerificationEngine
import com.docconverter.premium.core.verification.VerificationEngineImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module providing application-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the Room database instance
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.create(context)
    }

    /**
     * Provides the document repository implementation
     */
    @Provides
    @Singleton
    fun provideDocumentRepository(
        @ApplicationContext context: Context,
        database: AppDatabase
    ): DocumentRepository {
        return DocumentRepositoryImpl(context, database)
    }

    /**
     * Provides the verification engine implementation
     */
    @Provides
    @Singleton
    fun provideVerificationEngine(
        @ApplicationContext context: Context
    ): VerificationEngine {
        return VerificationEngineImpl(context)
    }

    /**
     * Provides the conversion service implementation
     */
    @Provides
    @Singleton
    fun provideConversionService(
        @ApplicationContext context: Context,
        documentRepository: DocumentRepository,
        verificationEngine: VerificationEngine
    ): ConversionService {
        return ConversionServiceImpl(context, documentRepository, verificationEngine)
    }
}
