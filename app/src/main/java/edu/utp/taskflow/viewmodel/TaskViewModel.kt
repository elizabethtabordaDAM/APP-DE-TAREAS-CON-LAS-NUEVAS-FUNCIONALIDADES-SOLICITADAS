package edu.utp.taskflow.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import edu.utp.taskflow.data.repository.TaskRepository
import edu.utp.taskflow.data.sampleTasks
import edu.utp.taskflow.model.Task
import edu.utp.taskflow.model.TaskFilter
import edu.utp.taskflow.model.TaskPriority


@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    private val repository: TaskRepository
): ViewModel() {

    // USUARIO ACTUAL (null = nadie con sesión iniciada)
    private val _currentUserId = MutableStateFlow<Int?>(null)

    // LISTA DE TAREAS (solo las del usuario actual)
    val tasks = _currentUserId
        .flatMapLatest { userId ->
            if (userId == null) {
                flowOf(emptyList<Task>())
            } else {
                repository.observeTasks(userId)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // FILTRO SELECCIONADO
    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    var selectedFilter = _selectedFilter.asStateFlow()


    // CAMBIO DE USUARIO (al iniciar o cerrar sesión)
    fun setCurrentUser(userId: Int?) {
        _currentUserId.value = userId
    }

    // CAMBIO DE FILTRO
    fun changeFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    // COMPLETAR O REABRIR TAREA
    fun toggleTask(taskId: Int) {
        viewModelScope.launch {
            repository.toggleTask(taskId)
        }
    }

    // ELIMINAR TAREA
    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    // AGREGAR TAREA (se guarda a nombre del usuario actual)
    fun addTask(
        title: String,
        priority: TaskPriority
    ){
        val userId = _currentUserId.value ?: return

        viewModelScope.launch {
            repository.addTask(userId, title, priority)
        }
    }

    // EDITAR TAREA
    fun updateTask(
        taskId: Int,
        title: String,
        priority: TaskPriority
    ) {
        viewModelScope.launch {
            repository.updateTask(taskId, title, priority)
        }
    }
}


class TaskViewModelFactory (
    private val repository: TaskRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(
            modelClass.isAssignableFrom(
                TaskViewModel::class.java
            )
        )

        return TaskViewModel(repository) as T
    }
}