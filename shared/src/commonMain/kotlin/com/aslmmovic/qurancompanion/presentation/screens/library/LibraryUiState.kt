package com.aslmmovic.qurancompanion.presentation.screens.library

import androidx.compose.runtime.Immutable
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import org.jetbrains.compose.resources.StringResource

@Immutable
data class LibraryUiState(
    val searchQuery: String = "",
    val selectedCategory: SahabaCategory = SahabaCategory.ALL,
    val sahabaList: List<Sahaba> = emptyList(),
    val selectedSahabaDetail: Sahaba? = null,
    val isLoading: Boolean = false,
    val errorMessage: StringResource? = null
)
