package com.aslmmovic.qurancompanion.presentation.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.domain.usecase.GetSahabaCatalogUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

abstract class BaseLibraryViewModel : ViewModel() {
    abstract val uiState: StateFlow<LibraryUiState>
    abstract fun onSearchQueryChanged(query: String)
    abstract fun onCategorySelected(category: SahabaCategory)
    abstract fun onSahabaSelected(sahaba: Sahaba)
    abstract fun onDismissDetail()
}

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModel(
    private val getSahabaCatalogUseCase: GetSahabaCatalogUseCase
) : BaseLibraryViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow(SahabaCategory.ALL)
    private val _selectedSahabaDetail = MutableStateFlow<Sahaba?>(null)

    override val uiState: StateFlow<LibraryUiState> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedSahabaDetail
    ) { query, category, detail ->
        Triple(query, category, detail)
    }.flatMapLatest { (query, category, detail) ->
        getSahabaCatalogUseCase(category = category, searchQuery = query).map { list ->
            LibraryUiState(
                searchQuery = query,
                selectedCategory = category,
                sahabaList = list,
                selectedSahabaDetail = detail,
                isLoading = false,
                errorMessage = null
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibraryUiState(isLoading = true)
    )

    override fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    override fun onCategorySelected(category: SahabaCategory) {
        _selectedCategory.value = category
    }

    override fun onSahabaSelected(sahaba: Sahaba) {
        _selectedSahabaDetail.value = sahaba
    }

    override fun onDismissDetail() {
        _selectedSahabaDetail.value = null
    }
}
