package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.Sahaba
import com.aslmmovic.qurancompanion.domain.repository.SahabaRepository

class GetSahabaDetailUseCase(
    private val repository: SahabaRepository
) {
    suspend operator fun invoke(id: String): Result<Sahaba> {
        return repository.getSahabaById(id)
    }
}
