package com.aslmmovic.qurancompanion.data.repository

import com.aslmmovic.qurancompanion.data.datasource.LocaleProvider
import com.aslmmovic.qurancompanion.data.datasource.SahabaLocalDataSource
import com.aslmmovic.qurancompanion.data.dto.SahabaDto
import com.aslmmovic.qurancompanion.domain.model.SahabaCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SahabaRepositoryImplTest {

    private val fakeDataSource = FakeSahabaLocalDataSource()
    private val fakeLocaleProvider = FakeLocaleProvider()

    private fun createRepository(dispatcher: kotlinx.coroutines.CoroutineDispatcher = UnconfinedTestDispatcher()) =
        SahabaRepositoryImpl(
            localDataSource = fakeDataSource,
            localeProvider = fakeLocaleProvider,
            ioDispatcher = dispatcher
        )

    @Test
    fun `test_AC01_givenBundledJson_whenGetCatalog_thenReturnsMappedSahabaList`() = runTest {
        val dtos = listOf(
            SahabaDto("abu_bakr", "Abu Bakr", "أبو بكر", "أ", "As-Siddiq", "TEN_PROMISED", 14, "Bio 1"),
            SahabaDto("umar", "Umar", "عمر", "ع", "Al-Faruq", "TEN_PROMISED", 12, "Bio 2"),
            SahabaDto("uthman", "Uthman", "عثمان", "ع", "Dhun-Nurayn", "TEN_PROMISED", 10, "Bio 3"),
            SahabaDto("ali", "Ali", "علي", "ع", "Lion of Allah", "TEN_PROMISED", 12, "Bio 4"),
            SahabaDto("talha", "Talha", "طلحة", "ط", "Living Martyr", "TEN_PROMISED", 8, "Bio 5"),
            SahabaDto("zubayr", "Zubayr", "الزبير", "ز", "Disciple", "TEN_PROMISED", 8, "Bio 6"),
            SahabaDto("abdur_rahman", "Abdur-Rahman", "عبد الرحمن", "ع", "Generous Merchant", "TEN_PROMISED", 7, "Bio 7"),
            SahabaDto("sad_ibn_abi_waqqas", "Sa'd", "سعد", "س", "Conqueror", "TEN_PROMISED", 8, "Bio 8"),
            SahabaDto("saeed_ibn_zayd", "Saeed", "سعيد", "س", "Pioneer", "TEN_PROMISED", 6, "Bio 9"),
            SahabaDto("abu_ubaidah", "Abu Ubaidah", "أبو عبيدة", "أ", "Trustee", "TEN_PROMISED", 8, "Bio 10"),
            SahabaDto("khadija", "Khadija", "خديجة", "خ", "Mother of Believers", "MOTHERS_OF_BELIEVERS", 10, "Bio 11"),
            SahabaDto("aisha", "Aisha", "عائشة", "ع", "Mother of Believers", "MOTHERS_OF_BELIEVERS", 11, "Bio 12"),
            SahabaDto("bilal", "Bilal", "بلال", "ب", "Mu'adhin", "MUHAJIRUN", 9, "Bio 13"),
            SahabaDto("fatima", "Fatima", "فاطمة", "ف", "Leader of Women", "MUHAJIRUN", 8, "Bio 14"),
            SahabaDto("abu_ayyub", "Abu Ayyub", "أبو أيوب", "أ", "Host of Prophet", "ANSAR", 7, "Bio 15"),
            SahabaDto("sad_ibn_muadh", "Sa'd ibn Mu'adh", "سعد بن معاذ", "س", "Leader of Ansar", "ANSAR", 7, "Bio 16")
        )
        fakeDataSource.catalog = dtos
        val repository = createRepository(UnconfinedTestDispatcher(testScheduler))

        val catalog = repository.getSahabaCatalog().first()

        assertEquals(16, catalog.size)
        assertEquals("abu_bakr", catalog[0].id)
        assertEquals(SahabaCategory.TEN_PROMISED, catalog[0].category)
        assertEquals("khadija", catalog[10].id)
        assertEquals(SahabaCategory.MOTHERS_OF_BELIEVERS, catalog[10].category)
        assertEquals("bilal", catalog[12].id)
        assertEquals(SahabaCategory.MUHAJIRUN, catalog[12].category)
        assertEquals("abu_ayyub", catalog[14].id)
        assertEquals(SahabaCategory.ANSAR, catalog[14].category)
    }

    private class FakeSahabaLocalDataSource : SahabaLocalDataSource {
        var catalog: List<SahabaDto> = emptyList()
        override suspend fun loadSahabaCatalog(locale: String): List<SahabaDto> = catalog
    }

    private class FakeLocaleProvider : LocaleProvider {
        private val _currentLocaleFlow = MutableStateFlow("en")
        override val currentLocaleFlow: StateFlow<String> = _currentLocaleFlow.asStateFlow()
        override var currentLocale: String
            get() = _currentLocaleFlow.value
            set(value) { _currentLocaleFlow.value = value }
        override fun changeLocale(locale: String) { currentLocale = locale }
    }
}
