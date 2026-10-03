package com.aslmmovic.qurancompanion.domain.repository

import com.aslmmovic.qurancompanion.domain.model.Sahaba
import kotlinx.coroutines.flow.Flow

interface SahabaRepository {
    fun getSahabaCatalog(): Flow<List<Sahaba>>
    suspend fun getSahabaById(id: String): Result<Sahaba>
}
