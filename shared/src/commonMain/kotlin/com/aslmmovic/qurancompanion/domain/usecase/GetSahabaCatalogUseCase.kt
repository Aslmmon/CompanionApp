package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.domain.repository.SahabaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetSahabaCatalogUseCase(
    private val repository: SahabaRepository
) {
    operator fun invoke(
        category: SahabaCategory = SahabaCategory.ALL,
        searchQuery: String = ""
    ): Flow<List<Sahaba>> {
        return repository.getSahabaCatalog().map { list ->
            list.filter { sahaba ->
                val matchesCategory = category == SahabaCategory.ALL || sahaba.category == category
                val matchesQuery = searchQuery.isBlank() ||
                    sahaba.name.contains(searchQuery, ignoreCase = true) ||
                    sahaba.arabicName.contains(searchQuery, ignoreCase = true) ||
                    sahaba.epithet.contains(searchQuery, ignoreCase = true)
                matchesCategory && matchesQuery
            }
        }
    }
}
