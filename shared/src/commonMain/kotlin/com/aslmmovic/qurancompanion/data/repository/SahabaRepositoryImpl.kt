package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.datasource.SahabaLocalDataSource
import com.aslmmovic.qurancompanion.data.dto.toDomain
import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.repository.SahabaRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SahabaRepositoryImpl(
    private val localDataSource: SahabaLocalDataSource,
    private val localeProvider: LocaleProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : SahabaRepository {

    override fun getSahabaCatalog(): Flow<List<Sahaba>> =
        localeProvider.currentLocaleFlow.map { locale ->
            withContext(ioDispatcher) {
                localDataSource.loadSahabaCatalog(locale).map { it.toDomain() }
            }
        }.flowOn(ioDispatcher)

    override suspend fun getSahabaById(id: String): Result<Sahaba> = withContext(ioDispatcher) {
        try {
            val locale = localeProvider.currentLocale
            val list = localDataSource.loadSahabaCatalog(locale)
            val sahabaDto = list.find { it.id == id }
            if (sahabaDto != null) {
                Result.success(sahabaDto.toDomain())
            } else {
                Result.failure(NoSuchElementException("Sahaba with id '$id' not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
