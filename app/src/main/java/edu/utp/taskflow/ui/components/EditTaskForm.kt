package edu.utp.taskflow.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.utp.taskflow.model.TaskPriority


@Composable
fun EditTaskForm(
    title: String,
    onTitleChange: (String) -> Unit,
    selectedPriority: TaskPriority,
    onPriorityChange: (TaskPriority) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Editar tarea", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("¿Qué necesitas hacer?") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Prioridad", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskPriority.entries.forEach { priority ->
                FilterChip(
                    selected = selectedPriority == priority,
                    onClick = { onPriorityChange(priority) },
                    label = { Text(when (priority) {
                        TaskPriority.HIGH -> "Alta"
                        TaskPriority.MEDIUM -> "Media"
                        TaskPriority.LOW -> "Baja"
                    }) }
                )
            }
        }
        Button(onClick = onSave, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Guardar cambios") }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
    }
}