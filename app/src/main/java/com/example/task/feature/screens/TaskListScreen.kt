package com.example.task.feature.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.task.data.AppDatabase
import com.example.task.feature.TaskViewModel
import com.example.task.feature.components.AddTaskSheet
import com.example.task.feature.components.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen() {
    val context = LocalContext.current

    val database = AppDatabase.getDatabase(context)
    val dao = database.taskDao()

    val viewModel = viewModel { TaskViewModel(dao) }

    val uiState by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    val groupedTasks = uiState.tasks.groupBy { it.category }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            AddTaskSheet(
                categories = uiState.categories,
                onAddTask = { newTask ->
                    viewModel.addTask(newTask)
                },
                onDismiss = { showAddSheet = false }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Список дел") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить задачу"
                )
            }
        }
    ) { padding ->
        if (uiState.tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Никаких задач нет :)",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                uiState.categories.forEach { category ->
                    val categoryTasks = groupedTasks[category] ?: emptyList()
                    if (categoryTasks.isNotEmpty()) {
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        items(categoryTasks) { task ->
                            TaskItem(
                                task = task,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateTask(task.copy(isCompleted = isChecked))
                                },
                                onDelete = {
                                    viewModel.deleteTask(task)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
