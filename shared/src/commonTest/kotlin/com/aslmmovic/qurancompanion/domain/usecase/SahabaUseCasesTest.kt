package com.aslmmovic.qurancompanion.domain.usecase

import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import com.aslmmovic.qurancompanion.fakes.FakeSahabaRepository
import com.aslmmovic.qurancompanion.fakes.testSahaba
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SahabaUseCasesTest {

    private lateinit var repository: FakeSahabaRepository
    private lateinit var getSahabaCatalogUseCase: GetSahabaCatalogUseCase
    private lateinit var getSahabaDetailUseCase: GetSahabaDetailUseCase

    @BeforeTest
    fun setUp() {
        val testCatalog = listOf(
            testSahaba(id = "abu_bakr", name = "Abu Bakr as-Siddiq", epithet = "As-Siddiq", category = SahabaCategory.TEN_PROMISED),
            testSahaba(id = "umar", name = "Umar ibn al-Khattab", epithet = "Al-Faruq", category = SahabaCategory.TEN_PROMISED),
            testSahaba(id = "khadija", name = "Khadija bint Khuwaylid", epithet = "Mother of Believers", category = SahabaCategory.MOTHERS_OF_BELIEVERS),
            testSahaba(id = "abu_ayyub", name = "Abu Ayyub al-Ansari", epithet = "Host of Prophet", category = SahabaCategory.ANSAR),
            testSahaba(id = "bilal", name = "Bilal ibn Rabah", epithet = "Mu'adhin ar-Rasul", category = SahabaCategory.MUHAJIRUN)
        )
        repository = FakeSahabaRepository(testCatalog)
        getSahabaCatalogUseCase = GetSahabaCatalogUseCase(repository)
        getSahabaDetailUseCase = GetSahabaDetailUseCase(repository)
    }

    @Test
    fun `test_AC02_givenCatalog_whenFilterByCategory_thenReturnsOnlyMatchingCategory`() = runTest {
        val result = getSahabaCatalogUseCase(category = SahabaCategory.TEN_PROMISED).first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.category == SahabaCategory.TEN_PROMISED })
        assertEquals("abu_bakr", result[0].id)
        assertEquals("umar", result[1].id)
    }

    @Test
    fun `test_AC03_givenCatalog_whenSearchQueryApplied_thenReturnsMatchingSahaba`() = runTest {
        val resultByName = getSahabaCatalogUseCase(searchQuery = "Abu Bakr").first()
        assertEquals(1, resultByName.size)
        assertEquals("abu_bakr", resultByName.first().id)

        val resultByEpithet = getSahabaCatalogUseCase(searchQuery = "Al-Faruq").first()
        assertEquals(1, resultByEpithet.size)
        assertEquals("umar", resultByEpithet.first().id)
    }

    @Test
    fun `test_AC04_givenValidId_whenGetSahabaDetail_thenReturnsSahaba`() = runTest {
        val result = getSahabaDetailUseCase("abu_bakr")

        assertTrue(result.isSuccess)
        val sahaba = result.getOrThrow()
        assertEquals("abu_bakr", sahaba.id)
        assertEquals("Abu Bakr as-Siddiq", sahaba.name)
    }

    @Test
    fun getSahabaCatalog_noFilters_returnsFullCatalog() = runTest {
        val result = getSahabaCatalogUseCase().first()
        assertEquals(5, result.size)
    }

    @Test
    fun getSahabaDetail_unknownId_returnsFailure() = runTest {
        val result = getSahabaDetailUseCase("unknown_id")
        assertTrue(result.isFailure)
    }

    @Test
    fun getSahabaCatalog_emptySearchQuery_returnsAll() = runTest {
        val result = getSahabaCatalogUseCase(searchQuery = "").first()
        assertEquals(5, result.size)

        val resultWhitespace = getSahabaCatalogUseCase(searchQuery = "   ").first()
        assertEquals(5, resultWhitespace.size)
    }

    @Test
    fun getSahabaCatalog_nonMatchingQuery_returnsEmptyList() = runTest {
        val result = getSahabaCatalogUseCase(searchQuery = "non_existent_query_xyz").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getSahabaCatalog_categoryAndSearchCombined_returnsIntersection() = runTest {
        val result = getSahabaCatalogUseCase(
            category = SahabaCategory.TEN_PROMISED,
            searchQuery = "Umar"
        ).first()
        assertEquals(1, result.size)
        assertEquals("umar", result.first().id)

        // Matching query but wrong category
        val wrongCategoryResult = getSahabaCatalogUseCase(
            category = SahabaCategory.ANSAR,
            searchQuery = "Umar"
        ).first()
        assertTrue(wrongCategoryResult.isEmpty())
    }
}
