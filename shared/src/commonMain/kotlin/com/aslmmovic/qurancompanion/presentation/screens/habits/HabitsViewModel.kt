package com.aslmmovic.qurancompanion.presentation.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslmmovic.qurancompanion.domain.model.Habit
import com.aslmmovic.qurancompanion.domain.model.HabitCategory
import com.aslmmovic.qurancompanion.domain.usecase.AddHabitFromCatalogUseCase
import com.aslmmovic.qurancompanion.domain.usecase.CreateCustomHabitUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetActiveHabitsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.GetCuratedHabitsUseCase
import com.aslmmovic.qurancompanion.domain.usecase.RemoveHabitUseCase
import com.aslmmovic.qurancompanion.domain.usecase.ToggleHabitCompletionUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class BaseHabitsViewModel : ViewModel() {
    abstract val uiState: StateFlow<HabitsUiState>
    abstract val uiEffect: Flow<HabitsUiEffect>
    abstract fun onToggleHabit(habitId: String)
    abstract fun onOpenAddSheet()
    abstract fun onCloseAddSheet()
    abstract fun onCatalogCategorySelected(category: HabitCategory)
    abstract fun onCatalogSearchQueryChanged(query: String)
    abstract fun onAddHabitFromCatalog(habitId: String)
    abstract fun onCreateCustomHabit(title: String, description: String, category: HabitCategory)
    abstract fun onSelectHabitForDetail(habit: Habit)
    abstract fun onDismissHabitDetail()
    abstract fun onRemoveHabit(habitId: String)
}

class HabitsViewModel(
    private val getActiveHabitsUseCase: GetActiveHabitsUseCase,
    private val getCuratedHabitsUseCase: GetCuratedHabitsUseCase,
    private val toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase,
    private val addHabitFromCatalogUseCase: AddHabitFromCatalogUseCase,
    private val createCustomHabitUseCase: CreateCustomHabitUseCase,
    private val removeHabitUseCase: RemoveHabitUseCase
) : BaseHabitsViewModel() {

    private val _isAddSheetOpen = MutableStateFlow(false)
    private val _catalogCategory = MutableStateFlow(HabitCategory.ALL)
    private val _catalogSearchQuery = MutableStateFlow("")
    private val _selectedHabitDetail = MutableStateFlow<Habit?>(null)

    private val _uiEffect = Channel<HabitsUiEffect>(Channel.BUFFERED)
    override val uiEffect: Flow<HabitsUiEffect> = _uiEffect.receiveAsFlow()

    private var previousAllCompleted = false

    override val uiState: StateFlow<HabitsUiState> = combine(
        getActiveHabitsUseCase(),
        _isAddSheetOpen,
        _catalogCategory,
        _catalogSearchQuery,
        _selectedHabitDetail
    ) { activeHabits, isAddOpen, category, query, detail ->
        Params(activeHabits, isAddOpen, category, query, detail)
    }.combine(getCuratedHabitsUseCase()) { params, allCatalog ->
        val filteredCatalog = allCatalog.filter { habit ->
            val matchesCategory = params.category == HabitCategory.ALL || habit.category == params.category
            val matchesQuery = params.query.isBlank() ||
                habit.title.contains(params.query, ignoreCase = true) ||
                habit.description.contains(params.query, ignoreCase = true)
            val notAlreadyActive = params.activeHabits.none { it.id == habit.id }
            matchesCategory && matchesQuery && notAlreadyActive
        }

        val completedCount = params.activeHabits.count { it.isCompletedToday }
        val totalCount = params.activeHabits.size
        val progressPercentage = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0
        val isAllDone = totalCount > 0 && completedCount == totalCount

        if (isAllDone && !previousAllCompleted) {
            _uiEffect.trySend(HabitsUiEffect.ShowCelebration)
        }
        previousAllCompleted = isAllDone

        HabitsUiState(
            activeHabits = params.activeHabits,
            completedCount = completedCount,
            totalCount = totalCount,
            progressPercentage = progressPercentage,
            isAllCompleted = isAllDone,
            isAddSheetOpen = params.isAddOpen,
            catalogHabits = filteredCatalog,
            selectedCatalogCategory = params.category,
            catalogSearchQuery = params.query,
            selectedHabitForDetail = params.detail,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HabitsUiState(isLoading = true)
    )

    override fun onToggleHabit(habitId: String) {
        viewModelScope.launch {
            toggleHabitCompletionUseCase(habitId)
        }
    }

    override fun onOpenAddSheet() {
        _isAddSheetOpen.value = true
    }

    override fun onCloseAddSheet() {
        _isAddSheetOpen.value = false
        _catalogSearchQuery.value = ""
        _catalogCategory.value = HabitCategory.ALL
    }

    override fun onCatalogCategorySelected(category: HabitCategory) {
        _catalogCategory.value = category
    }

    override fun onCatalogSearchQueryChanged(query: String) {
        _catalogSearchQuery.value = query
    }

    override fun onAddHabitFromCatalog(habitId: String) {
        viewModelScope.launch {
            addHabitFromCatalogUseCase(habitId)
            onCloseAddSheet()
        }
    }

    override fun onCreateCustomHabit(title: String, description: String, category: HabitCategory) {
        viewModelScope.launch {
            createCustomHabitUseCase(title, description, category)
            onCloseAddSheet()
        }
    }

    override fun onSelectHabitForDetail(habit: Habit) {
        _selectedHabitDetail.value = habit
    }

    override fun onDismissHabitDetail() {
        _selectedHabitDetail.value = null
    }

    override fun onRemoveHabit(habitId: String) {
        viewModelScope.launch {
            removeHabitUseCase(habitId)
            onDismissHabitDetail()
        }
    }

    private data class Params(
        val activeHabits: List<Habit>,
        val isAddOpen: Boolean,
        val category: HabitCategory,
        val query: String,
        val detail: Habit?
    )
}
