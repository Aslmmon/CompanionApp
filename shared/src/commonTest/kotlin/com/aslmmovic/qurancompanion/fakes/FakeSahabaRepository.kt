package com.aslmmovic.qurancompanion.fakes

import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.repository.SahabaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSahabaRepository(
    initialCatalog: List<Sahaba> = emptyList()
) : SahabaRepository {

    private val catalogFlow = MutableStateFlow(initialCatalog)

    fun setCatalog(catalog: List<Sahaba>) {
        catalogFlow.value = catalog
    }

    override fun getSahabaCatalog(): Flow<List<Sahaba>> = catalogFlow.asStateFlow()

    override suspend fun getSahabaById(id: String): Result<Sahaba> {
        val sahaba = catalogFlow.value.find { it.id == id }
        return if (sahaba != null) {
            Result.success(sahaba)
        } else {
            Result.failure(NoSuchElementException("Sahaba with id '$id' not found"))
        }
    }
}
