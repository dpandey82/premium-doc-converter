package com.docconverter.premium.ui.home

import androidx.lifecycle.ViewModel
import com.docconverter.premium.core.models.Document
import com.docconverter.premium.core.storage.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ViewModel for the home screen
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    /**
     * Recent documents flow
     */
    val recentDocuments: Flow<List<Document>> = documentRepository.getRecentDocuments(10)

    /**
     * Conversion statistics
     */
    private val _conversionStats = MutableStateFlow(ConversionStats(0, "0 MB"))
    val conversionStats: StateFlow<ConversionStats> = _conversionStats

    init {
        // In a real implementation, would fetch actual stats from a repository
        _conversionStats.value = ConversionStats(
            totalConversions = 12,
            savedStorageFormatted = "45 MB"
        )
    }
}

/**
 * Data class representing conversion statistics
 */
data class ConversionStats(
    val totalConversions: Int,
    val savedStorageFormatted: String
)
