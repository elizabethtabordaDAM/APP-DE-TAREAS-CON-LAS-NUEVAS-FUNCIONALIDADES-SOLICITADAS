package edu.utp.taskflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ModalBottomSheet

import edu.utp.taskflow.data.sampleTasks
import edu.utp.taskflow.model.Task
import edu.utp.taskflow.model.TaskFilter
import edu.utp.taskflow.model.TaskPriority
import edu.utp.taskflow.ui.screens.TaskFlowScreen
import edu.utp.taskflow.ui.theme.TaskFlowPalette
import edu.utp.taskflow.ui.theme.TaskFlowTheme
import edu.utp.taskflow.ui.components.NewTaskForm
import edu.utp.taskflow.ui.components.EditTaskForm
import edu.utp.taskflow.viewmodel.TaskViewModel

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import edu.utp.taskflow.data.local.TaskDatabase
import edu.utp.taskflow.data.repository.TaskRepository
import edu.utp.taskflow.data.repository.UserRepository
import edu.utp.taskflow.ui.screens.TaskDetailScreen
import edu.utp.taskflow.ui.screens.LoginScreen
import edu.utp.taskflow.ui.screens.RegisterScreen

import edu.utp.taskflow.viewmodel.TaskViewModelFactory
import edu.utp.taskflow.viewmodel.AuthViewModel
import edu.utp.taskflow.viewmodel.AuthViewModelFactory


private object TaskRoutes {
    const val LOGIN = "login"

    const val REGISTER = "register"

    const val LIST = "tasks"

    const val DETAIL = "task_detail/{taskId}"

    fun detail(taskId: Int): String {
        return "task_detail/$taskId"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = TaskDatabase.getDatabase(applicationContext)

        val dao = database.taskDao()
        val repository = TaskRepository(dao)
        val factory = TaskViewModelFactory(repository)

        val userRepository = UserRepository(database.userDao())
        val authFactory = AuthViewModelFactory(userRepository)

        setContent {
            val taskViewModel: TaskViewModel = viewModel(factory = factory)
            val authViewModel: AuthViewModel = viewModel(factory = authFactory)

            TaskFlowDemo(taskViewModel, authViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFlowDemo(
    taskViewModel: TaskViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val tasks by taskViewModel.tasks.collectAsStateWithLifecycle()
    val selectedFilter by taskViewModel.selectedFilter.collectAsStateWithLifecycle()

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val authError by authViewModel.errorMessage.collectAsStateWithLifecycle()

    // Único estado interactivo de la base visual: cambiar la paleta.
    // Las operaciones sobre tareas siguen pendientes para la clase.
    var palette by remember { mutableStateOf(TaskFlowPalette.OCEAN) }

    var showNewTaskSheet by remember {
        mutableStateOf(false)
    }

    var newTaskTitle by remember {
        mutableStateOf("")
    }

    var newTaskPriority by remember {
        mutableStateOf(TaskPriority.MEDIUM)
    }

    // Tarea que se está editando (null = no se está editando ninguna)
    var taskBeingEdited by remember {
        mutableStateOf<Task?>(null)
    }

    var editTaskTitle by remember {
        mutableStateOf("")
    }

    var editTaskPriority by remember {
        mutableStateOf(TaskPriority.MEDIUM)
    }

    val navController = rememberNavController()

    // Navegación según la sesión:
    // - Si inicia sesión (o se registra) desde login/registro, va a sus tareas.
    // - Si cierra sesión (o no hay sesión), vuelve al login.
    LaunchedEffect(currentUser) {
        val route = navController.currentDestination?.route
            ?: return@LaunchedEffect

        val onAuthScreen = route == TaskRoutes.LOGIN || route == TaskRoutes.REGISTER

        if (currentUser != null && onAuthScreen) {
            navController.navigate(TaskRoutes.LIST) {
                popUpTo(TaskRoutes.LOGIN) { inclusive = true }
            }
        } else if (currentUser == null && !onAuthScreen) {
            navController.navigate(TaskRoutes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }
    LaunchedEffect(currentUser) {
        taskViewModel.setCurrentUser(currentUser?.id)
    }

    TaskFlowTheme(palette = palette) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = TaskRoutes.LOGIN
            ) {

                // Pantalla de inicio de sesión
                composable(TaskRoutes.LOGIN) {
                    LoginScreen(
                        errorMessage = authError,
                        onLogin = authViewModel::login,
                        onGoToRegister = {
                            authViewModel.clearError()
                            navController.navigate(TaskRoutes.REGISTER)
                        }
                    )
                }

                // Pantalla de registro
                composable(TaskRoutes.REGISTER) {
                    RegisterScreen(
                        errorMessage = authError,
                        onRegister = authViewModel::register,
                        onGoToLogin = {
                            authViewModel.clearError()
                            navController.popBackStack()
                        }
                    )
                }

                // Pantalla principal
                composable(TaskRoutes.LIST) {
                    TaskFlowScreen(
                        tasks = tasks,
                        selectedFilter = selectedFilter,
                        onFilterChange = taskViewModel::changeFilter,
                        onToggleTask = taskViewModel::toggleTask,
                        onDeleteTask = taskViewModel::deleteTask,
                        onEditTask = { task ->
                            editTaskTitle = task.title
                            editTaskPriority = task.priority
                            taskBeingEdited = task
                        },
                        onTaskClick = { taskId ->
                            navController.navigate(
                                TaskRoutes.detail(taskId)
                            )
                        },
                        onNewTaskClick = {
                            newTaskTitle = ""
                            newTaskPriority = TaskPriority.MEDIUM
                            showNewTaskSheet = true
                        },
                        palette = palette,
                        onPaletteChange = { palette = it },
                        username = currentUser?.username ?: "",
                        onLogout = authViewModel::logout
                    )
                }

                // Pantalla de detalle
                composable(
                    route = TaskRoutes.DETAIL,
                    arguments = listOf(
                        navArgument("taskId") {
                            type = NavType.IntType
                        }
                    )
                ) { backStackEntry ->
                    val taskId = backStackEntry.arguments?.getInt("taskId")

                    val selectedTask = tasks.firstOrNull {
                        it.id == taskId
                    }

                    TaskDetailScreen(
                        task = selectedTask,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        if (showNewTaskSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showNewTaskSheet = false
                }
            ) {
                NewTaskForm(
                    title = newTaskTitle,
                    onTitleChange = { newTaskTitle = it },
                    selectedPriority = newTaskPriority,
                    onPriorityChange = { newTaskPriority = it },
                    onSave = {
                        if (newTaskTitle.isNotBlank()) {
                            taskViewModel.addTask(
                                title = newTaskTitle,
                                priority = newTaskPriority
                            )

                            showNewTaskSheet = false
                        }
                    },
                    onCancel = {
                        showNewTaskSheet = false
                    }
                )
            }
        }

        // Hoja inferior para editar una tarea existente
        taskBeingEdited?.let { editingTask ->
            ModalBottomSheet(
                onDismissRequest = {
                    taskBeingEdited = null
                }
            ) {
                EditTaskForm(
                    title = editTaskTitle,
                    onTitleChange = { editTaskTitle = it },
                    selectedPriority = editTaskPriority,
                    onPriorityChange = { editTaskPriority = it },
                    onSave = {
                        if (editTaskTitle.isNotBlank()) {
                            taskViewModel.updateTask(
                                taskId = editingTask.id,
                                title = editTaskTitle,
                                priority = editTaskPriority
                            )

                            taskBeingEdited = null
                        }
                    },
                    onCancel = {
                        taskBeingEdited = null
                    }
                )
            }
        }

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TaskFlowPreview() {
    TaskFlowDemo()
}